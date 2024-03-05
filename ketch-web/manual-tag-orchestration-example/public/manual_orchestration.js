function loadMarketingTags() {
    // Check if tag has already been loaded
    if (document.getElementById('marketing-script')) return;

    // Create script tag
    var marketingScript = document.createElement('script');
    marketingScript.id = 'marketing-script';
    marketingScript.type = 'text/javascript';

    // Set sourec of script tag
    marketingScript.src = 'https://third-party-service/script.js'

    // Append script tag to head
    document.head.appendChild(marketingScript);
}

function loadAnalyticsTags() {
    // Check if tag has already been loaded
    if (document.getElementById('analytics-script')) return;

    // Create script tag
    var analyticsScript = document.createElement('script');
    analyticsScript.id = 'analytics-script';
    analyticsScript.type = 'text/javascript';

    // Set inline script to contents of third party script
    var inlineScript = document.createTextNode(`
    (function () { 
        alert('Analytics tags loaded');
    })();
    `);

    // Append inline script to script tag
    analyticsScript.appendChild(inlineScript);

    // Append script tag to head
    document.head.appendChild(analyticsScript);
}

function loadEssentialServicesTags() {
    // Check if tag has already been loaded
    if (document.getElementById('essential-script')) return;

    // Create script tag
    var essentialScript = document.createElement('script');
    essentialScript.id = 'essential-script';
    essentialScript.type = 'text/javascript';

    // Set inline script to contents of third party script
    var inlineScript = document.createTextNode(`alert('Essential services tags loaded');`);

    // Append inline script to script tag
    essentialScript.appendChild(inlineScript);

    // Append script tag to head
    document.head.appendChild(essentialScript);
}

function loadTagsOnPage(purposes, purposesKeys) {
    purposesKeys.forEach(key => {
        switch(key) {
            case 'essential_services':
                // Load essential services tags if visitor has given consent
                if (purposes[key] === true) {
                    loadEssentialServicesTags();
                }
                break;
            case 'analytics':
                // Load analytics tags if visitor has given consent
                if (purposes[key] === true) {
                    loadAnalyticsTags();
                }
                break;
            case 'marketing':
                // Load marketing tags if visitor has given consent
                if (purposes[key] === true) {
                    loadMarketingTags();
                }
                break;
        }
    });
}

// Add event listener to be called anytime permit values are loaded 
//  or when an end-user updates their consent.
window.ketch('on', 'consent', function(c) { 
    console.log(c);
    console.log(c.purposes);
    var puporsesKeys = Object.keys(c.purposes);
    loadTagsOnPage(c.purposes, puporsesKeys);
})