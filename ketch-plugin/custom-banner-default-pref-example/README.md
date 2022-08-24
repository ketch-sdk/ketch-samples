# Custom banner with default preference experience
This sample shows the flexibility of being able to show a custom consent and disclosure banner, while still using the default privacy preferences experience.

To do this, you create a custome plugin to interact with the Ketch Smart Tag. The plugin architecture gives you access to the same functionality to get and update consent just like the default Ketch Experience.

To facilitate showing the consent & disclosure banner at the correct time, create a function and assign it to the `showConsentExperience` property on the plugin object.

```javascript
var custom_plugin = {
    'showConsentExperience': function(host, config, consent) {
        ...
    }
}
```

> _Note: To disable the default Ketch Consent & Disclosure Experience from showing, please contact your account executive, or [Ketch Support](mailto:support@ketch.com)._