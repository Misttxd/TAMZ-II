const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

(async () => {
  const browser = await chromium.launch({
    executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
    headless: true
  });
  const page = await browser.newPage({ viewport: { width: 1360, height: 1040 } });
  const errors = [];
  page.on('pageerror', e => errors.push(e.message));
  const url = pathToFileURL(path.join(__dirname, '..', 'pruvodce-aplikaci.html')).href;
  await page.goto(url);
  const data = await page.locator('#guideData').evaluate(e => JSON.parse(e.textContent));
  assert.equal(data.screens.length, 7);
  assert.equal(Object.keys(data.components).length, 35);
  assert.equal(await page.locator('.catalog-item').count(), 35);
  assert.equal(await page.locator('#screenImage').evaluate(e => e.naturalWidth), 1080);

  await page.locator('.hotspot[aria-label="Výsledek — SavingsSummary"]').hover();
  assert.equal(await page.locator('#componentName').textContent(), 'SavingsSummary()');
  assert.deepEqual(await page.locator('#codeBlock .code-line').allTextContents(), data.components.SavingsSummary.code.split('\n'));
  await page.locator('#zoomImage').click();
  assert.equal(await page.locator('#imageDialog').evaluate(e => e.open), true);
  await page.locator('#zoomedImage').evaluate(e => e.decode());
  assert.equal(await page.locator('#zoomedImage').evaluate(e => e.naturalWidth), 1080);
  await page.keyboard.press('Escape');
  await page.locator('#copyCode').click();
  assert.equal(await page.locator('#copyCode').textContent(), 'Zkopírováno');
  await page.screenshot({ path: path.join(__dirname, 'nahled-desktop.png') });

  for (const screen of data.screens) {
    await page.locator('.screen-tab[data-screen="'+screen.id+'"]').click();
    await page.locator('#screenImage').evaluate(e => e.decode());
    assert.equal(await page.locator('#screenImage').evaluate(e => e.naturalHeight), 2424);
    assert.equal(await page.locator('.hotspot').count(), screen.regions.length);
    await page.locator('#screenCode').click();
    assert.equal(await page.locator('#componentName').textContent(), screen.parent+'()');
  }

  await page.locator('#search').fill('ChoiceRow');
  assert.equal(await page.locator('.catalog-item').count(), 1);
  await page.locator('.catalog-item').click();
  assert.equal(await page.locator('#componentName').textContent(), 'ChoiceRow()');
  assert.equal(await page.locator('#screenTitle').textContent(), 'Nastavení');
  await page.locator('#search').fill('neexistující komponenta');
  assert.equal(await page.locator('.empty').count(), 1);
  await page.locator('#search').fill('');
  await page.locator('#fullSource').click();
  assert.equal(await page.locator('#sourceDialog').evaluate(e => e.open), true);
  await page.keyboard.press('Escape');
  assert.equal(await page.locator('#sourceDialog').evaluate(e => e.open), false);

  await page.setViewportSize({ width: 390, height: 844 });
  await page.locator('.screen-tab[data-screen="kalkulacka"]').click();
  await page.evaluate(() => window.scrollTo(0, 0));
  assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1), 'Obsah přetéká na mobilu.');
  await page.screenshot({ path: path.join(__dirname, 'nahled-mobil.png'), fullPage: true });
  await page.locator('.hotspot[aria-label="Měsíční vklad — SavingsSlider"]').focus();
  assert.equal(await page.locator('#componentName').textContent(), 'SavingsSlider()');
  assert.deepEqual(errors, []);
  await browser.close();
  console.log('OK: 7 screenshotů, 35 vysvětlení, hover, klávesnice, vyhledávání, vazby, zdrojový kód a mobilní rozložení.');
})().catch(e => { console.error(e); process.exit(1); });
