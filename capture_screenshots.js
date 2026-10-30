const puppeteer = require('/tmp/screenshot_tool/node_modules/puppeteer-core');
const path = require('path');

const CHROME_PATH = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const OUT_DIR = path.resolve(__dirname, 'screenshots');
const BASE_URL = 'http://localhost:8080';

async function run() {
    console.log('Launching headless Chrome...');
    const browser = await puppeteer.launch({
        executablePath: CHROME_PATH,
        headless: 'new',
        args: [
            '--no-sandbox',
            '--disable-setuid-sandbox',
            '--disable-gpu',
            '--window-size=1440,960'
        ]
    });

    const page = await browser.newPage();
    await page.setViewport({ width: 1440, height: 960, deviceScaleFactor: 2 });

    const sleep = (ms) => new Promise(r => setTimeout(r, ms));

    // 1. Home Page
    console.log('Navigating to Home Page...');
    await page.goto(`${BASE_URL}/#home`, { waitUntil: 'networkidle0' });
    await sleep(1000);
    await page.screenshot({ path: path.join(OUT_DIR, '01_home_page.png'), fullPage: true });
    console.log('Captured: 01_home_page.png');

    // 2. Browse Items
    console.log('Navigating to Browse Items...');
    await page.evaluate(() => App.navigate('browse'));
    await sleep(1000);
    await page.screenshot({ path: path.join(OUT_DIR, '02_browse_items.png'), fullPage: true });
    console.log('Captured: 02_browse_items.png');

    // 3. Login Modal
    console.log('Opening Login Modal...');
    await page.evaluate(() => App.openLoginModal());
    await sleep(600);
    await page.screenshot({ path: path.join(OUT_DIR, '03_login_modal.png') });
    console.log('Captured: 03_login_modal.png');
    await page.evaluate(() => App.closeModal('loginModal'));
    await sleep(400);

    // 4. Register Modal
    console.log('Opening Register Modal...');
    await page.evaluate(() => App.openRegisterModal());
    await sleep(600);
    await page.screenshot({ path: path.join(OUT_DIR, '04_register_modal.png') });
    console.log('Captured: 04_register_modal.png');
    await page.evaluate(() => App.closeModal('registerModal'));
    await sleep(400);

    // 5. Report Lost Item
    console.log('Navigating to Report Lost Item...');
    await page.evaluate(() => App.navigate('report', { type: 'LOST' }));
    await sleep(800);
    await page.screenshot({ path: path.join(OUT_DIR, '05_report_lost_item.png'), fullPage: true });
    console.log('Captured: 05_report_lost_item.png');

    // 6. Report Found Item
    console.log('Navigating to Report Found Item...');
    await page.evaluate(() => App.navigate('report', { type: 'FOUND' }));
    await sleep(800);
    await page.screenshot({ path: path.join(OUT_DIR, '06_report_found_item.png'), fullPage: true });
    console.log('Captured: 06_report_found_item.png');

    // 7. Item Details Modal
    console.log('Opening Item Details Modal...');
    await page.evaluate(() => {
        if (typeof App.showItemDetailModal === 'function') {
            App.showItemDetailModal(1);
        }
    });
    await sleep(800);
    await page.screenshot({ path: path.join(OUT_DIR, '07_item_details_modal.png') });
    console.log('Captured: 07_item_details_modal.png');
    await page.evaluate(() => App.closeModal('itemDetailModal'));
    await sleep(400);

    // 8. Log in as Student Suyash
    console.log('Logging in as student suyash@campus.edu...');
    await page.evaluate(async () => {
        await API.login('suyash@campus.edu', 'Student@123');
    });
    await sleep(1000);

    // 9. Student Dashboard
    console.log('Navigating to Student Dashboard...');
    await page.evaluate(() => App.navigate('dashboard'));
    await sleep(1000);
    await page.screenshot({ path: path.join(OUT_DIR, '08_student_dashboard.png'), fullPage: true });
    console.log('Captured: 08_student_dashboard.png');

    // 10. Smart Matches
    console.log('Navigating to Smart Matches...');
    await page.evaluate(() => App.navigate('matches'));
    await sleep(1000);
    await page.screenshot({ path: path.join(OUT_DIR, '09_smart_matches.png'), fullPage: true });
    console.log('Captured: 09_smart_matches.png');

    // 11. Claims Center
    console.log('Navigating to Claims Center...');
    await page.evaluate(() => App.navigate('claims'));
    await sleep(1000);
    await page.screenshot({ path: path.join(OUT_DIR, '10_claims_center.png'), fullPage: true });
    console.log('Captured: 10_claims_center.png');

    // 12. Notifications Dropdown
    console.log('Opening Notifications Dropdown...');
    await page.evaluate(() => App.toggleNotificationsDropdown());
    await sleep(600);
    await page.screenshot({ path: path.join(OUT_DIR, '11_notifications_dropdown.png') });
    console.log('Captured: 11_notifications_dropdown.png');
    await page.evaluate(() => App.toggleNotificationsDropdown());
    await sleep(400);

    // 13. Log in as Admin
    console.log('Logging out student and logging in as Admin...');
    await page.evaluate(async () => {
        API.logout();
        await API.login('admin@findit.campus.edu', 'Admin@123');
    });
    await sleep(1000);

    // 14. Admin Analytics Dashboard
    console.log('Navigating to Admin Analytics Dashboard...');
    await page.evaluate(() => App.navigate('admin'));
    await sleep(2000); // Wait for Chart.js animation
    await page.screenshot({ path: path.join(OUT_DIR, '12_admin_analytics_dashboard.png'), fullPage: true });
    console.log('Captured: 12_admin_analytics_dashboard.png');

    await browser.close();
    console.log('Successfully captured all 12 page views and saved to screenshots/ directory!');
}

run().catch(err => {
    console.error('Screenshot capture failed:', err);
    process.exit(1);
});

