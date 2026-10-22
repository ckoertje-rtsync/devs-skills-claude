#!/usr/bin/env python3
"""Detect DEVS model source files and report language, framework, kind and models.

Usage: detect-devs-model.py <file-or-dir> [<file-or-dir> ...]

Prints a Markdown table with one row per candidate source file:

    | file | language | framework | kind | models |

followed by a `summary:` line. Detection is regex-based and heuristic; it is
meant to seed the devs-doxygen skill's DISCOVER stage, not to replace reading
the source. Used by the skill, safe to call standalone.
"""
import os
import re
import sys

EXT_LANG = {
    '.java': 'java',
    '.cpp': 'cpp', '.cc': 'cpp', '.cxx': 'cpp', '.hpp': 'cpp', '.hh': 'cpp', '.h': 'cpp',
    '.py': 'python',
    '.jl': 'julia',
}

FRAMEWORKS = {
    'java': [
        ('closure-devs', r'com\.ms4systems\.devs'),
        ('devsjava', r'\b(GenCol|genDevs|ViewableAtomic|ViewableDigraph|simView)\b'),
        ('xdevs-java', r'\bxdevs\.core\b'),
        ('paradevs', r'ParaDevsNodeFor'),
    ],
    'cpp': [
        ('adevs', r'\badevs\b'),
        ('cadmium', r'\bcadmium\b'),
        ('xdevs-cpp', r'\bxdevs\b'),
    ],
    'python': [
        ('pypdevs', r'\bpypdevs\b'),
        ('xdevs-python', r'\bxdevs\b'),
    ],
    'julia': [],
}

ATOMIC_BASES = {
    'Atomic', 'AtomicDEVS', 'ContinuousTimeMarkov', 'ViewableAtomic', 'atomic',
    'AbstractAtomic', 'AtomicModel', 'PhaseBased', 'StateVariableBased',
}
COUPLED_BASES = {
    'Coupled', 'CoupledDEVS', 'ViewableDigraph', 'digraph', 'Digraph', 'SimpleDigraph',
    'Network', 'AbstractCoupled', 'CoupledModel',
}

DEVS_FUNCS = re.compile(
    r'\b(delta_int|deltint|internal_transition|intTransition|internalTransition|'
    r'delta_ext|deltext|external_transition|extTransition|externalTransition|'
    r'delta_conf?|deltcon|confluent_transition|confTransition|confluentTransition|'
    r'output_func|outputFnc|getOutput|lambdaf|lambda|'
    r'time_advance|timeAdvance|getTimeAdvance|ta)\s*!?\s*\('
)
COUPLING_FUNCS = re.compile(
    r'\b(addCoupling|connectPorts|add_coupling|couple|addIC|addEIC|addEOC|addSubModel|'
    r'addChildModel|addComponent|add_component|make_dynamic_coupled_model|ic\s*=|eic\s*=|eoc\s*=)\b'
)

CLASS_RES = {
    'java': re.compile(r'\b(?:public\s+|abstract\s+|final\s+)*class\s+(\w+)(?:\s+extends\s+([\w.]+))?'),
    'cpp': re.compile(r'\b(?:class|struct)\s+(\w+)\s*(?:final\s*)?:\s*(?:public\s+|protected\s+|private\s+)?([\w:]+)'),
    'python': re.compile(r'^\s*class\s+(\w+)\s*\(\s*([\w.]+)', re.MULTILINE),
    'julia': re.compile(r'^\s*(?:mutable\s+)?struct\s+(\w+)(?:\{[^}]*\})?\s*(?:<:\s*([\w.]+))?', re.MULTILINE),
}


def classify_base(base):
    """Map a base-class name to 'atomic', 'coupled' or None."""
    if not base:
        return None
    simple = base.replace('::', '.').split('.')[-1].split('<')[0]
    if simple in ATOMIC_BASES or 'Atomic' in simple:
        return 'atomic'
    if simple in COUPLED_BASES or 'Coupled' in simple or 'Digraph' in simple:
        return 'coupled'
    return None


def analyze(path):
    lang = EXT_LANG.get(os.path.splitext(path)[1].lower())
    if not lang:
        return None
    try:
        with open(path, encoding='utf-8', errors='replace') as fh:
            text = fh.read()
    except OSError:
        return None

    framework = 'generic-' + lang
    for name, pat in FRAMEWORKS[lang]:
        if re.search(pat, text):
            framework = name
            break

    n_devs_funcs = len(set(m.group(1) for m in DEVS_FUNCS.finditer(text)))
    has_couplings = bool(COUPLING_FUNCS.search(text))

    models = []
    for m in CLASS_RES[lang].finditer(text):
        name, base = m.group(1), m.group(2)
        kind = classify_base(base)
        if kind is None:
            if lang == 'julia':
                mutable = re.search(r'mutable\s+struct\s+' + re.escape(name) + r'\b', text)
                kind = 'atomic?' if (mutable and n_devs_funcs >= 3) else 'other'
            elif has_couplings:
                kind = 'coupled?'
            elif n_devs_funcs >= 3:
                kind = 'atomic?'
            else:
                kind = 'other'
        models.append((name, kind))

    kinds = {k.rstrip('?') for _, k in models if k != 'other'}
    if not kinds and n_devs_funcs >= 3:
        kinds = {'atomic'}
    if not kinds and has_couplings:
        kinds = {'coupled'}
    if not kinds and not models:
        return None  # not a DEVS source file
    kind = '+'.join(sorted(kinds)) if kinds else 'unknown'
    return {
        'file': path, 'language': lang, 'framework': framework, 'kind': kind,
        'models': models, 'devs_funcs': n_devs_funcs,
    }


def collect(paths):
    files = []
    for p in paths:
        if os.path.isdir(p):
            for root, dirs, names in os.walk(p):
                dirs[:] = [d for d in dirs if not d.startswith('.') and d not in ('node_modules', 'build', 'target', 'html')]
                for n in sorted(names):
                    files.append(os.path.join(root, n))
        else:
            files.append(p)
    return files


def main(argv):
    if len(argv) < 2:
        sys.stderr.write(__doc__)
        return 2
    rows = [r for r in (analyze(f) for f in collect(argv[1:])) if r]
    print('| file | language | framework | kind | models |')
    print('|---|---|---|---|---|')
    for r in rows:
        models = ', '.join('%s (%s)' % (n, k) for n, k in r['models']) or '-'
        print('| %s | %s | %s | %s | %s |' % (r['file'], r['language'], r['framework'], r['kind'], models))
    langs = sorted({r['language'] for r in rows})
    fws = sorted({r['framework'] for r in rows})
    n_atomic = sum(1 for r in rows for _, k in r['models'] if k.startswith('atomic'))
    n_coupled = sum(1 for r in rows for _, k in r['models'] if k.startswith('coupled'))
    print()
    print('summary: %d file(s), languages=%s, frameworks=%s, atomic=%d, coupled=%d'
          % (len(rows), ','.join(langs) or '-', ','.join(fws) or '-', n_atomic, n_coupled))
    return 0 if rows else 1


if __name__ == '__main__':
    sys.exit(main(sys.argv))
