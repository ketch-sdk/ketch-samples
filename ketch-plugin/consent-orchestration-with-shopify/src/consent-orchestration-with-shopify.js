var cosPlugin = {
    'init': (host) => {
    },
    'consentChanged': (host, config, consent) => {
        if (consent.purposes.analytics === true) {
            window.Shopify.customerPrivacy.setTrackingConsent(true);
        } else {
            window.Shopify.customerPrivacy.setTrackingConsent(false);
        }
    }
}

function initKetchShopifyCustomIntegration() {
    window.semaphore = window.semaphore || [];
    window.semaphore.push(['registerPlugin', cosPlugin]);
}
window.Shopify.loadFeatures([
    {
      name: 'consent-tracking-api',
      version: '0.1',
    }
  ],
  function(error) {
    if (error) {
      throw error;
    }

    initKetchShopifyCustomIntegration();
  });