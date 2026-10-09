#!/usr/bin/env node
// Headless check of the demo page against its bundle (devs-demo skill).
// Runs the page's inline script in a VM without a DOM: the script must expose its pure
// functions on window.<GLOBAL_API> and return before touching document.
'use strict';
const fs = require('fs'), path = require('path'), vm = require('vm');

const PAGE = '__PAGE__.html';            // TODO file name under demos/
const API = '__API__';                   // TODO e.g. NTNDemo
const DATA = '__DATA__';                 // TODO e.g. NTN_DEMO
const ROOT = '__ROOT__';                 // TODO root node of the path, e.g. Transmitter_GS
const label = n => n.replace(/_(CellSAT|SatCTM)$/, '');   // TODO label rule
// TODO known answers: scenario -> expected path (labels joined by '>')
const KNOWN_PATHS = [
  // { ef: 'X_Band', res: 'Base', tf: null, path: 'Transmitter_GS>X0Y0>X0Y1>X0Y2>Receiver_GS' },
];

const dir = path.resolve(__dirname, '..');
const html = fs.readFileSync(path.join(dir, PAGE), 'utf8');
const dataJs = fs.readFileSync(path.join(dir, 'data', 'demo-data.js'), 'utf8');
const m = /<script>([\s\S]*?)<\/script>/.exec(html);
if (!m) { console.error('no inline <script> found'); process.exit(1); }
const ctx = { window: {}, console };
vm.createContext(ctx);
vm.runInContext(dataJs, ctx, { filename: 'demo-data.js' });
vm.runInContext(m[1], ctx, { filename: PAGE });
const api = ctx.window[API], b = ctx.window[DATA];

let failures = 0, checks = 0;
const ok = (c, msg) => { checks++; if (!c) { failures++; console.log('FAIL  ' + msg); } };
const close = (a, b, rel, msg) => {
  if ((a == null || !isFinite(a)) && (b == null || !isFinite(b))) return ok(true, msg);
  ok(typeof a === 'number' && typeof b === 'number' && Math.abs(a - b) <= rel * Math.max(Math.abs(a), Math.abs(b), 1e-300), msg + ' (' + a + ' vs ' + b + ')');
};
const sc = (ef, res, tf) => b.scenarios.find(s => s.ef === ef && s.resolution === res && (s.tFlare == null ? tf == null : s.tFlare === tf));

ok(api && typeof api.pathTaken === 'function', 'page exposes ' + API);
const problems = api.validateBundle(b);
ok(problems.length === 0, 'validateBundle: ' + problems.join('; '));
b.scenarios.forEach(s => ok(s.stats.n === b.meta.reps, s.ef + '/' + s.resolution + ': stats.n == meta.reps'));

b.scenarios.forEach(s => {
  const topo = api.buildTopo(b.topology[s.resolution]);
  const key = s.ef + '/' + s.resolution + '/' + s.tFlare;
  s.trace.steps.forEach((step, k) => {
    step.msgs.forEach(mm => {
      const link = api.linkFor(mm.from, mm.to, topo);
      const tray = mm.to === 'TransdFlare';   // TODO declared exceptions (messages drawn as a pulse, not a token)
      ok(link || tray, key + ' step ' + k + ': unmapped message ' + mm.from + '.' + mm.port + ' -> ' + mm.to);
    });
    const touched = new Set(step.events.map(e => e.model));
    Object.keys(step.after).forEach(n => ok(touched.has(n), key + ' step ' + k + ': after has untouched ' + n));
  });
  const pt = api.pathTaken(s.trace.steps);
  if (s.result.success) {
    ok(pt.deliveryStep != null, key + ': delivery step found');
    if (pt.deliveryStep != null) close(s.trace.steps[pt.deliveryStep].t - (s.result.tStart || 0), s.result.tReport, 1e-9, key + ': delivery step t == TReport');
  } else ok(pt.deliveryStep == null, key + ': no delivery step');
  const states = api.materialize(s.trace);
  s.trace.steps.forEach((step, k) => {
    const lines = api.describeStep(s, states, k, topo);
    ok(lines.length === step.events.length, key + ' step ' + k + ': one line per event');
    step.events.forEach((e, i) => ok(lines[i] && lines[i].indexOf(label(e.model)) === 0, 'line names its model: ' + (lines[i] || '')));
  });
});

KNOWN_PATHS.forEach(k => {
  const s = sc(k.ef, k.res, k.tf);
  ok(s, 'known scenario present: ' + k.ef + '/' + k.res + '/' + k.tf);
  if (s) { const p = api.pathTaken(s.trace.steps).path.map(label).join('>'); ok(p === k.path, k.ef + '/' + k.res + ' path: ' + p); }
});

ok(api.fmtT(null) === '∞' && api.fmtT(Infinity) === '∞' && api.fmtT(1e308) === '∞', 'fmtT infinities');
ok(api.fmtT(0) === '0' && api.fmtT(2.4e-4) === '2.40e-4' && api.fmtT(10) === '10', 'fmtT values');
ok(!/https?:\/\//.test(html.replace(/http:\/\/www\.w3\.org\/2000\/svg/g, '')), 'no external URLs in the page');
ok(/<title>[^<]{3,60}<\/title>/.test(html.slice(0, 8192)), 'title within the first 8 KB');
ok(/prefers-color-scheme: dark\)\s*\{\s*:root:not\(\[data-theme="light"\]\)/.test(html) && /:root\[data-theme="dark"\]\s*\{/.test(html), 'both dark theme blocks present');

console.log(checks + ' checks, ' + failures + ' failures; ' + b.scenarios.length + ' scenarios, ' + b.meta.reps + ' reps, generated ' + b.meta.generated);
process.exit(failures ? 1 : 0);
