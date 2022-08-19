# Custom plugin to for manual consent orchestration to Shopify
When you move from Shopify's built in tracking consent management to Ketch, there maybe some Shopify tags loaded on the page without your control. Using the Shopify JS library already loaded on the page, you can set the tracking consent within the Shopify platform.

This sample loads the Ketch Smart Tag on the page, displaying the configured consent banner within the Ketch Platform.

Upon receiving the consent change, the plugin will call the necessary Shopify library method to update the tracking consent within the platform.

_NOTE: this example is using a mock Shopify library built using the information provided by Shopify on [this page](https://shopify.dev/api/consent-tracking?shpxid=8d6a6a23-6F53-448F-E864-6BBFA5774C4B#settrackingconsent-consent-boolean-callback-function)._

## Getting started