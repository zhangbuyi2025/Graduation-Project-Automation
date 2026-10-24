#!/usr/bin/env node
/**
 * SRS DOCX builder — standalone generator for the
 * lang-to-ppt-to-fullstack-ppt skill.
 *
 * Usage:
 *   node gen-srs.js <content.json> <output.docx>
 *
 * The JSON spec mirrors the SRS structure:
 * {
 *   "title": "校园图书借阅管理系统",
 *   "subtitle": "软件需求规格说明书",
 *   "version": "V1.0",
 *   "organization": "编制单位",
 *   "date": "2026-10-22",
 *   "header": "校园图书借阅管理系统 — 软件需求规格说明书",
 *   "revisions": [["V1.0", "2026-10-22", "初稿", "编制组"]],
 *   "chapters": [
 *     { "heading": "1 引言",
 *       "sections": [
 *         { "heading": "1.1 目的",
 *           "blocks": [
 *             { "type": "p", "text": "正文段落" },
 *             { "type": "bullets", "items": ["条目一", "条目二"] },
 *             { "type": "numbers", "items": ["步骤一", "步骤二"] },
 *             { "type": "table", "header": ["列1", "列2"], "rows": [["a", "b"]] },
 *             { "type": "image", "text": "【此处插入图片：用例图——读者借阅流程】" }
 *           ] }
 *       ] }
 *   ]
 * }
 *
 * Dependencies: docx (installed in <SKILL_DIR>/internal/deps/node_modules).
 * Run from anywhere: the module is resolved through NODE_PATH or a local install.
 */

const fs = require('fs');
const path = require('path');

function loadDocx() {
  const candidates = [
    process.env.SKILL_DOCX_MODULE,
    path.resolve(__dirname, '..', 'deps', 'node_modules', 'docx'),
    'docx',
  ].filter(Boolean);
  for (const candidate of candidates) {
    try {
      return require(candidate);
    } catch (err) {
      if (err && err.code !== 'MODULE_NOT_FOUND') throw err;
    }
  }
  throw new Error(
    'Cannot resolve the "docx" package. Run: npm install --prefix "<SKILL_DIR>/internal/deps"'
  );
}

const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  WidthType, AlignmentType, HeadingLevel, BorderStyle, ShadingType,
  Header, Footer, PageNumber, LevelFormat, TableOfContents,
  convertInchesToTwip,
} = loadDocx();

const BODY_FONT = '宋体';
const HEADING_FONT = '黑体';
const BODY_SIZE = 24;          // half-points → 12pt
const TABLE_WIDTH_DXA = 9026;  // A4 (11906) − 2 × 1440 margins
const HEADER_FILL = '1F4E79';

const numbering = {
  config: [
    {
      reference: 'srs-bullets',
      levels: [
        {
          level: 0,
          format: LevelFormat.BULLET,
          text: '\u2022',
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720, hanging: 360 } } },
        },
      ],
    },
    {
      reference: 'srs-numbers',
      levels: [
        {
          level: 0,
          format: LevelFormat.DECIMAL,
          text: '%1.',
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720, hanging: 360 } } },
        },
      ],
    },
  ],
};

const thinBorder = { style: BorderStyle.SINGLE, size: 4, color: 'CCCCCC' };
const tableBorders = {
  top: thinBorder, bottom: thinBorder, left: thinBorder, right: thinBorder,
  insideHorizontal: thinBorder, insideVertical: thinBorder,
};

function bodyRun(text, opts = {}) {
  return new TextRun({
    text: String(text == null ? '' : text),
    font: BODY_FONT,
    size: BODY_SIZE,
    bold: Boolean(opts.bold),
    color: opts.color,
  });
}

function paragraph(text, opts = {}) {
  return new Paragraph({
    children: [bodyRun(text, opts)],
    alignment: opts.alignment,
    spacing: { before: 60, after: 120, line: 360 },
    indent: opts.indent,
  });
}

function bulletList(items, reference) {
  return items.map((item) => new Paragraph({
    children: [bodyRun(item)],
    numbering: { reference, level: 0 },
    spacing: { before: 40, after: 40, line: 360 },
  }));
}

function tableCell(text, { header = false, columnIndex = 0, columnCount = 1 } = {}) {
  const width = Math.floor(TABLE_WIDTH_DXA / columnCount);
  return new TableCell({
    width: { size: width, type: WidthType.DXA },
    shading: header
      ? { type: ShadingType.CLEAR, fill: HEADER_FILL, color: 'auto' }
      : undefined,
    margins: { top: 60, bottom: 60, left: 100, right: 100 },
    children: [
      new Paragraph({
        children: [bodyRun(text, { bold: header, color: header ? 'FFFFFF' : undefined })],
        spacing: { before: 0, after: 0 },
        alignment: header ? AlignmentType.CENTER : AlignmentType.LEFT,
      }),
    ],
    columnSpan: undefined,
  });
}

function buildTable(header, rows) {
  const columnCount = Math.max(header.length, ...rows.map((r) => r.length), 1);
  const headRow = new TableRow({
    tableHeader: true,
    children: header.map((cell, i) => tableCell(cell, { header: true, columnIndex: i, columnCount })),
  });
  const bodyRows = rows.map((row) => new TableRow({
    children: Array.from({ length: columnCount }, (_, i) =>
      tableCell(row[i], { columnIndex: i, columnCount })),
  }));
  return new Table({
    width: { size: TABLE_WIDTH_DXA, type: WidthType.DXA },
    columnWidths: Array.from({ length: columnCount }, () => Math.floor(TABLE_WIDTH_DXA / columnCount)),
    borders: tableBorders,
    rows: [headRow, ...bodyRows],
  });
}

function blockToElements(block) {
  switch (block.type) {
    case 'p':
      return [paragraph(block.text)];
    case 'bullets':
      return bulletList(block.items || [], 'srs-bullets');
    case 'numbers':
      return bulletList(block.items || [], 'srs-numbers');
    case 'table':
      return [buildTable(block.header || [], block.rows || []), paragraph('')];
    case 'image':
      return [paragraph(block.text, { bold: true, alignment: AlignmentType.CENTER })];
    default:
      throw new Error(`Unknown block type: ${block.type}`);
  }
}

function headingParagraph(text, level) {
  return new Paragraph({
    heading: level,
    children: [new TextRun({ text, font: HEADING_FONT, bold: true, size: level === HeadingLevel.HEADING_1 ? 32 : 28 })],
    spacing: { before: 240, after: 120 },
  });
}

function coverPage(spec) {
  const blank = (count) => Array.from({ length: count }, () => paragraph(''));
  return [
    ...blank(6),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      children: [new TextRun({ text: spec.title || '', font: HEADING_FONT, bold: true, size: 56 })],
      spacing: { after: 240 },
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      children: [new TextRun({ text: spec.subtitle || '软件需求规格说明书', font: HEADING_FONT, size: 40 })],
      spacing: { after: 1200 },
    }),
    ...blank(4),
    paragraph(`版本：${spec.version || 'V1.0'}`, { alignment: AlignmentType.CENTER }),
    paragraph(`编制单位：${spec.organization || ''}`, { alignment: AlignmentType.CENTER }),
    paragraph(`编制日期：${spec.date || ''}`, { alignment: AlignmentType.CENTER }),
    new Paragraph({ children: [new TextRun({ text: '', break: 1 })], pageBreakBefore: true }),
  ];
}

function revisionPage(spec) {
  const rows = spec.revisions || [];
  const elements = [headingParagraph('修订记录', HeadingLevel.HEADING_1)];
  elements.push(buildTable(['版本', '日期', '修订说明', '修订人'], rows));
  elements.push(new Paragraph({ children: [new TextRun({ text: '', break: 1 })], pageBreakBefore: true }));
  return elements;
}

function tocPage() {
  return [
    headingParagraph('目录', HeadingLevel.HEADING_1),
    new TableOfContents('目录', { hyperlink: true, headingStyleRange: '1-3' }),
    new Paragraph({ children: [new TextRun({ text: '', break: 1 })], pageBreakBefore: true }),
  ];
}

function buildDocument(spec) {
  const children = [
    ...coverPage(spec),
    ...revisionPage(spec),
    ...tocPage(),
  ];
  (spec.chapters || []).forEach((chapter) => {
    children.push(headingParagraph(chapter.heading, HeadingLevel.HEADING_1));
    (chapter.sections || []).forEach((section) => {
      if (section.heading) children.push(headingParagraph(section.heading, HeadingLevel.HEADING_2));
      (section.blocks || []).forEach((block) => children.push(...blockToElements(block)));
    });
  });

  return new Document({
    numbering,
    styles: {
      default: {
        document: { run: { font: BODY_FONT, size: BODY_SIZE } },
      },
    },
    sections: [{
      properties: {
        page: {
          size: { width: 11906, height: 16838 },
          margin: {
            top: convertInchesToTwip(1), right: convertInchesToTwip(1),
            bottom: convertInchesToTwip(1), left: convertInchesToTwip(1),
          },
        },
      },
      headers: {
        default: new Header({
          children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [new TextRun({ text: spec.header || spec.title || '', font: BODY_FONT, size: 20, color: '555555' })],
          })],
        }),
      },
      footers: {
        default: new Footer({
          children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [
              new TextRun({ text: '第 ', font: BODY_FONT, size: 20 }),
              new TextRun({ children: [PageNumber.CURRENT], font: BODY_FONT, size: 20 }),
              new TextRun({ text: ' 页 / 共 ', font: BODY_FONT, size: 20 }),
              new TextRun({ children: [PageNumber.TOTAL_PAGES], font: BODY_FONT, size: 20 }),
              new TextRun({ text: ' 页', font: BODY_FONT, size: 20 }),
            ],
          })],
        }),
      },
      children,
    }],
  });
}

async function main() {
  const [specPath, outPath] = process.argv.slice(2);
  if (!specPath || !outPath) {
    console.error('Usage: node gen-srs.js <content.json> <output.docx>');
    process.exit(2);
  }
  const spec = JSON.parse(fs.readFileSync(specPath, 'utf8'));
  const doc = buildDocument(spec);
  const buffer = await Packer.toBuffer(doc);
  fs.mkdirSync(path.dirname(path.resolve(outPath)), { recursive: true });
  fs.writeFileSync(outPath, buffer);
  console.log(`written: ${outPath} (${buffer.length} bytes)`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
