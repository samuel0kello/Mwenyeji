const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

const serviceAccount = require('./serviceAccountKey.json');
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
const db = admin.firestore();

const SKIPPED_CONTRIBUTOR_IDS = new Set(['gtfs_system_id', 'gtfs_seed']);

function cleanCsvField(val) {
    if (val === undefined || val === null) return '""';
    const cleaned = String(val)
        .replace(/[\r\n]+/g, ' ')
        .replace(/"/g, '""');
    return `"${cleaned}"`;
}

function isContributorRouteGuideCandidate(routeData) {
    if (!routeData || typeof routeData !== 'object') return false;

    const contributorId = routeData.contributorId;
    if (!contributorId || typeof contributorId !== 'string') return false;
    if (SKIPPED_CONTRIBUTOR_IDS.has(contributorId)) return false;

    return [
        'fareKsh',
        'bestTimeOfDay',
        'timingReason',
        'steps',
        'warnings',
        'tags',
        'source',
        'terminus'
    ].some((key) => routeData[key] !== undefined);
}

function buildGuideExportRow({ guideId, routeId, routeData, guideData = {} }) {
    const contributorIdValue = guideData.contributorId ?? routeData?.contributorId;
    const guidePayload = guideData && Object.keys(guideData).length > 0 ? guideData : routeData || {};

    const guideIdCsv = cleanCsvField(guideId);
    const routeIdCsv = cleanCsvField(routeId);
    const source = cleanCsvField(routeData?.source);
    const terminus = cleanCsvField(routeData?.terminus);
    const contributorId = cleanCsvField(contributorIdValue);

    const fare = cleanCsvField(guidePayload.fareKsh ?? routeData?.fareKsh);
    const bestTime = cleanCsvField(guidePayload.bestTimeOfDay ?? routeData?.bestTimeOfDay);
    const timingReason = cleanCsvField(guidePayload.timingReason ?? routeData?.timingReason);
    const warnings = cleanCsvField(guidePayload.warnings ?? routeData?.warnings);

    const steps = guidePayload.steps ?? routeData?.steps;
    const stepsCount = Array.isArray(steps) ? steps.length : 0;

    const narrativeSteps = Array.isArray(steps)
        ? steps.map((step) => {
            const instruction = typeof step === 'string'
                ? step
                : step?.instruction || step?.text || step?.detail || step?.description || '';
            return instruction ? instruction : '';
        }).filter(Boolean)
        : [];

    const guideNarrative = narrativeSteps.length > 0
        ? narrativeSteps.join(' | ')
        : [timingReason, warnings].filter(Boolean).join(' ');

    const tags = Array.isArray(guidePayload.tags ?? routeData?.tags)
        ? (guidePayload.tags ?? routeData?.tags).join('|')
        : '';

    return `${guideIdCsv},${routeIdCsv},${source},${terminus},${contributorId},${fare},${bestTime},${timingReason},${stepsCount},${warnings},${tags},${cleanCsvField(guideNarrative)}\n`;
}

async function exportStreamedGuides() {
    const writeStream = fs.createWriteStream(path.join(__dirname, 'cleaned_user_guides.csv'), 'utf8');

    writeStream.write('guideId,routeId,source,terminus,contributorId,fareKsh,bestTimeOfDay,timingReason,stepsCount,warnings,tags,guideNarrative\n');

    try {
        console.log('Beginning export (Deep Search Mode)...');

        let count = 0;
        let matchedRoutes = 0;
        const routesSnapshot = await db.collection('routes').get();
        console.log(`Found ${routesSnapshot.size} total routes to check.`);

        for (const routeDoc of routesSnapshot.docs) {
            const routeId = routeDoc.id;
            const routeData = routeDoc.data();
            const guidesSnapshot = await routeDoc.ref.collection('guides').get();
            const hasGuideSubcollection = !guidesSnapshot.empty;
            const hasContributorRouteGuide = isContributorRouteGuideCandidate(routeData);

            if (!hasGuideSubcollection && !hasContributorRouteGuide) continue;

            matchedRoutes += 1;

            if (hasGuideSubcollection) {
                for (const guideDoc of guidesSnapshot.docs) {
                    const guideData = guideDoc.data();
                    if (SKIPPED_CONTRIBUTOR_IDS.has(guideData.contributorId)) continue;

                    writeStream.write(buildGuideExportRow({
                        guideId: guideDoc.id,
                        routeId,
                        routeData,
                        guideData
                    }));
                    count += 1;
                }
            }

            if (!hasGuideSubcollection && hasContributorRouteGuide) {
                writeStream.write(buildGuideExportRow({
                    guideId: routeId,
                    routeId,
                    routeData,
                    guideData: routeData
                }));
                count += 1;
            }

            if (count > 0 && count % 10 === 0) {
                console.log(`Progress: Found ${count} guides so far...`);
            }
        }

        writeStream.end();
        console.log(`Export finished! Successfully wrote ${count} records to cleaned_user_guides.csv`);
        console.log(`Examined ${matchedRoutes} contributor-backed routes or guide collections.`);

        if (count === 0) {
            console.log('\n--- Troubleshooting ---');
            console.log('Checked all routes but found no contributor-backed guides.');
            console.log('Verifying if routes actually exist in Firestore...');
            if (routesSnapshot.empty) {
                console.log('The "routes" collection itself is EMPTY.');
            } else {
                console.log(`Found ${routesSnapshot.size} routes, but none matched contributor-backed guide data.`);
            }
        }

    } catch (error) {
        console.error('An error occurred during extraction:', error);
        writeStream.end();
    }
}

if (require.main === module) {
    exportStreamedGuides();
}

module.exports = {
    exportStreamedGuides,
    isContributorRouteGuideCandidate,
    buildGuideExportRow
};
