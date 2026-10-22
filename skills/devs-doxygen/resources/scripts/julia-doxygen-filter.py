#!/usr/bin/env python3
"""Doxygen input filter: Julia -> Python-flavoured pseudo-source.

Doxygen has no Julia parser. This filter rewrites the structural skeleton of a
Julia file (modules, structs and their fields, abstract types, functions,
macros, constants, and the docstrings that precede them) into text that
Doxygen's Python scanner accepts. Function bodies are emitted as comments;
nothing is executed. The output is for Doxygen only and is not valid Python.

Wire it up from a Doxyfile:

    EXTENSION_MAPPING = jl=Python
    FILE_PATTERNS     = *.jl *.dox
    FILTER_PATTERNS   = *.jl="python3 /abs/path/julia-doxygen-filter.py"

Usage: python3 julia-doxygen-filter.py <file.jl>   (writes to stdout)

Mapping summary
    module Name ... end                -> ## @namespace Name   (no indentation change)
    [mutable] struct Name{T} <: Super  -> class Name(Super):
        field::Type [= default]        ->     ## Julia type: Type / field = default
    abstract type Name <: Super end    -> class Name(Super): pass
    function name(args) ... end        -> def name(args): pass  (body as comments)
    name(args) = expr                  -> def name(args): pass  # = expr
    macro name(args) ... end           -> def name(args):  (documented as a macro)
    const NAME = value                 -> NAME = value
    \"\"\"docstring\"\"\" before a definition -> ## docstring lines (attached to it)
    Trailing '!' in a function name is kept; a dotted qualifier (Base.show) is
    dropped and noted in the doc comment.
"""
import os
import re
import sys

OPENER_RE = re.compile(
    r'^\s*(?:mutable\s+struct|struct|abstract\s+type|primitive\s+type|module|baremodule|'
    r'function|macro|if|for|while|begin|let|try|quote)\b'
)
TRAILING_OPENER_RE = re.compile(r'\b(do|begin|let|quote)\s*$')
TRAILING_END_RE = re.compile(r'\bend\s*$')
ONLY_END_RE = re.compile(r'^\s*end\s*$')

MODULE_RE = re.compile(r'^\s*(?:bare)?module\s+([A-Za-z_]\w*)')
STRUCT_RE = re.compile(r'^\s*(mutable\s+)?struct\s+([A-Za-z_]\w*)(\{[^}]*\})?\s*(?:<:\s*([A-Za-z_][\w.]*)(\{[^}]*\})?)?')
ABSTRACT_RE = re.compile(r'^\s*abstract\s+type\s+([A-Za-z_]\w*)(\{[^}]*\})?\s*(?:<:\s*([A-Za-z_][\w.]*)(\{[^}]*\})?)?\s*end\s*$')
PRIMITIVE_RE = re.compile(r'^\s*primitive\s+type\s+([A-Za-z_]\w*)(\{[^}]*\})?\s*(?:<:\s*([A-Za-z_][\w.]*))?\s+\d+\s*end\s*$')
FUNCTION_RE = re.compile(r'^\s*function\s+([A-Za-z_][\w.]*!?)\s*(\{[^}]*\})?\s*\((.*)\)\s*(?:::\s*[^\s]+)?\s*(?:where\b.*)?$')
FUNCTION_STUB_RE = re.compile(r'^\s*function\s+([A-Za-z_][\w.]*!?)\s*end\s*$')
CALLABLE_RE = re.compile(r'^\s*function\s+\(\s*\w+\s*::\s*([A-Za-z_][\w.]*)\s*\)\s*\((.*)\)')
MACRO_RE = re.compile(r'^\s*macro\s+([A-Za-z_]\w*)\s*\((.*)\)')
SHORT_FN_RE = re.compile(r'^\s*([A-Za-z_][\w.]*!?)\s*(\{[^}]*\})?\s*\((.*)\)\s*(?:::\s*[^\s=]+)?\s*(?:where\s*\{[^}]*\}\s*)?=(?!=)\s*(.*)$')
CONST_RE = re.compile(r'^\s*const\s+([A-Za-z_]\w*)\s*(?:::\s*[^\s=]+)?\s*=\s*(.*)$')
FIELD_RE = re.compile(r'^\s*([A-Za-z_]\w*)\s*(?:::\s*([^=]+?))?\s*(?:=\s*(.*))?\s*$')
IMPORT_RE = re.compile(r'^\s*(using|import|export|include)\b')
ONE_LINE_DOC_RE = re.compile(r'^"([^"]*)"\s*$')
KEYWORDS = {'end', 'else', 'elseif', 'catch', 'finally', 'return', 'break', 'continue', 'local', 'global'}


def strip_comment(line):
    """Drop a trailing '# ...' comment (string-unaware, good enough for structure)."""
    idx = line.find('#')
    return line if idx < 0 else line[:idx]


def py_args(args):
    """Julia argument list -> something Doxygen's Python lexer tolerates."""
    args = args.replace(';', ',')
    args = re.sub(r'\s*::\s*[^,=)]+', '', args)   # drop ::Type annotations
    args = re.sub(r'\s*\.\.\.', '', args)          # drop splats
    args = re.sub(r'\s+', ' ', args).strip(' ,')
    return args


def simple_name(name):
    """Drop a dotted qualifier: Base.show -> show. Returns (name, qualifier or None)."""
    if '.' in name:
        qual, _, base = name.rpartition('.')
        return base, qual
    return name, None


class Filter:
    def __init__(self, stem='module'):
        self.stem = stem
        self.out = []
        self.stack = []          # entries: 'class' | 'def' | 'other' | 'module'
        self.pending_doc = []    # docstring lines waiting for a definition
        self.in_doc = False
        self.doc_buf = []

    # ---- helpers -------------------------------------------------------
    def indent(self):
        return '    ' * sum(1 for k in self.stack if k in ('class', 'def'))

    def emit(self, text, extra=0):
        self.out.append('    ' * extra + self.indent() + text)

    def flush_doc(self, extra_notes=()):
        for note in extra_notes:
            self.emit('## ' + note)
        for line in self.pending_doc:
            self.emit('## ' + line if line.strip() else '##')
        self.pending_doc = []

    def discard_doc(self):
        for line in self.pending_doc:
            self.emit('# ' + line)
        self.pending_doc = []

    def in_scope(self, kind):
        return bool(self.stack) and self.stack[-1] == kind

    # ---- docstrings ----------------------------------------------------
    def handle_docstring(self, line):
        """Returns True if the line was consumed as (part of) a docstring."""
        s = line.strip()
        if self.in_doc:
            if '"""' in s:
                before = s[:s.index('"""')]
                if before.strip():
                    self.doc_buf.append(before)
                self.pending_doc = self.doc_buf
                self.doc_buf = []
                self.in_doc = False
            else:
                self.doc_buf.append(line.rstrip('\n').strip())
            return True
        m = ONE_LINE_DOC_RE.match(s)
        if m and not s.startswith('"""'):
            self.pending_doc = [m.group(1).strip()]
            return True
        if s.startswith('"""'):
            rest = s[3:]
            if '"""' in rest:                     # one-line docstring
                self.pending_doc = [rest[:rest.index('"""')].strip()]
                return True
            self.in_doc = True
            self.doc_buf = [rest.strip()] if rest.strip() else []
            return True
        return False

    # ---- main dispatch -------------------------------------------------
    def process(self, line):
        raw = line.rstrip('\n')
        if self.handle_docstring(raw):
            return
        code = strip_comment(raw)
        s = code.strip()

        if not s:
            if raw.strip().startswith('#'):
                self.emit('# ' + raw.strip().lstrip('#').strip())
            else:
                self.out.append('')
            return

        # 'end' on its own line closes the innermost block.
        if ONLY_END_RE.match(code):
            if self.stack:
                self.stack.pop()
            return

        # Single-line type definitions.
        m = ABSTRACT_RE.match(code)
        if m:
            name, sup = m.group(1), m.group(3)
            self.flush_doc()
            self.emit('class %s(%s):' % (name, simple_name(sup)[0]) if sup else 'class %s:' % name)
            self.emit('pass', 1)
            return
        m = PRIMITIVE_RE.match(code)
        if m:
            self.flush_doc(['Julia primitive type'])
            self.emit('class %s:' % m.group(1))
            self.emit('pass', 1)
            return

        opens = bool(OPENER_RE.match(code)) or bool(TRAILING_OPENER_RE.search(code))
        closes_inline = bool(TRAILING_END_RE.search(code)) and opens

        m = MODULE_RE.match(code)
        if m:
            # Doxygen names the Python "namespace" after the file stem, so the
            # module docstring is attached to that namespace.
            self.flush_doc(['@namespace ' + self.stem, '@brief Julia module `%s`' % m.group(1), ''])
            if not closes_inline:
                self.stack.append('module')
            return

        m = STRUCT_RE.match(code)
        if m:
            name, sup = m.group(2), m.group(4)
            notes = ['Julia %sstruct' % ('mutable ' if m.group(1) else '')]
            self.flush_doc(notes)
            self.emit('class %s(%s):' % (name, simple_name(sup)[0]) if sup else 'class %s:' % name)
            if closes_inline:
                self.emit('pass', 1)
            else:
                self.stack.append('class')
                self.emit('pass')
            return

        m = FUNCTION_STUB_RE.match(code)
        if m:
            name, qual = simple_name(m.group(1))
            self.flush_doc(['Julia generic function stub%s' % (' (%s.%s)' % (qual, name) if qual else '')])
            self.emit('def %s():' % name)
            self.emit('pass', 1)
            return

        m = CALLABLE_RE.match(code)
        if m:
            self.flush_doc(['Callable object of type %s' % m.group(1)])
            self.emit('def __call__(%s):' % py_args(m.group(2)))
            self.stack.append('def')
            self.emit('pass')
            return

        m = FUNCTION_RE.match(code)
        if m:
            name, qual = simple_name(m.group(1))
            notes = ['Julia method of %s.%s' % (qual, name)] if qual else []
            self.flush_doc(notes)
            self.emit('def %s(%s):' % (name, py_args(m.group(3))))
            if closes_inline:
                self.emit('pass', 1)
            else:
                self.stack.append('def')
                self.emit('pass')
            return

        m = MACRO_RE.match(code)
        if m:
            self.flush_doc(['Julia macro @%s' % m.group(1)])
            self.emit('def %s(%s):' % (m.group(1), py_args(m.group(2))))
            if closes_inline:
                self.emit('pass', 1)
            else:
                self.stack.append('def')
                self.emit('pass')
            return

        # Inside a function body: everything is a comment, but keep block depth.
        if self.in_scope('def') or self.in_scope('other'):
            self.discard_doc()
            self.emit('# ' + s)
            if opens and not closes_inline:
                self.stack.append('other')
            return

        m = CONST_RE.match(code)
        if m:
            self.flush_doc()
            self.emit('%s = %s' % (m.group(1), m.group(2).strip()))
            return

        m = SHORT_FN_RE.match(code)
        if m and m.group(1) not in KEYWORDS and not m.group(1)[0].isupper() or (m and self.in_scope('class')):
            name, qual = simple_name(m.group(1))
            notes = ['Julia method of %s.%s' % (qual, name)] if qual else []
            self.flush_doc(notes)
            self.emit('def %s(%s):' % (name, py_args(m.group(3))))
            self.emit('pass  # = ' + m.group(4).strip(), 1)
            return
        if m and m.group(1)[0].isupper() and not self.in_scope('class'):
            # Short-form outer constructor:  Name(args) = Name(...)
            name, qual = simple_name(m.group(1))
            self.flush_doc(['Outer constructor for %s' % name])
            self.emit('def %s(%s):' % (name, py_args(m.group(3))))
            self.emit('pass  # = ' + m.group(4).strip(), 1)
            return

        if IMPORT_RE.match(code):
            self.discard_doc()
            self.emit('# ' + s)
            return

        if self.in_scope('class'):
            fm = FIELD_RE.match(code)
            if fm and fm.group(1) not in KEYWORDS:
                name, typ, default = fm.group(1), fm.group(2), fm.group(3)
                notes = ['Julia type: ' + typ.strip()] if typ else []
                self.flush_doc(notes)
                self.emit('%s = %s' % (name, default.strip() if default else 'None'))
                return
            self.discard_doc()
            self.emit('# ' + s)
            if opens and not closes_inline:
                self.stack.append('other')
            return

        # Top-level statement that is not a definition.
        self.discard_doc()
        self.emit('# ' + s)
        if opens and not closes_inline:
            self.stack.append('other')

    def run(self, text):
        for line in text.splitlines():
            self.process(line)
        self.discard_doc()
        return '\n'.join(self.out) + '\n'


def main(argv):
    if len(argv) != 2:
        sys.stderr.write('usage: julia-doxygen-filter.py <file.jl>\n')
        return 2
    stem = os.path.splitext(os.path.basename(argv[1]))[0]
    with open(argv[1], encoding='utf-8', errors='replace') as fh:
        sys.stdout.write(Filter(stem).run(fh.read()))
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
