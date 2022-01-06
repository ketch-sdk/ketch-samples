# Custom Plugin to Interact with Ketch Platform
There are times when you need to interact with the Ketch Platform without rendering the default Ketch Experiences.

To do this, you create a custom plugin to interact with the Ketch Smart Tag. The plugin architecture gives you access to the same functionality to get and update consent just like the default Ketch Experience.

This sample displays a checkbox displaying a users current consent and allowing them to update their preferences.


> _Note: To disable the default Ketch Experience from showing, please contact your account executive, or [Ketch Support](mailto:support@ketch.com)._

To see all available events supported on the Ketch Plugin, check out this [repository](https://github.com/ketch-sdk/ketch-plugin/blob/main/src/index.ts).

## Getting started
To get you going creating your first Ketch plugin, you will need to go through all the steps have the Ketch Platform 

- Install Ketch Smart Tag support
```
npm install @ketch-sdk/ketch-tag
```

- Create a new TypeScript file for the plugin
```
touch custom.ts
```

- Create a property of type `Plugin`
``` TypeScript
const OptOutPlugin: Plugin = {
    init(host) {
        ...
    }
}
``` 

The `init` property is the only required property. It gives you access to the Ketch object used to get and update consent changes back to the Ketch Platform, as well as the configuration data for your organization on the Ketch Platform.

- Get current consent status
```TypeScript
let _consents: Consent;

const OptOutPlugin: Plugin = {
    init(host) {
        
        ...

        host.getConsent().then(consent => { 
            _consents = consent; 
            UpdateOptOutCheckBox(); 
        });

        ...

    }
}
```

- Update Ketch Platform with changes in consent
```TypeScript
let _host: Ketch
let _consents: Consent;

const OptOutPlugin: Plugin = {
    init(host) {
        
        ...

        // Add event listener to opt-out button
        let elem = document.getElementById('opt-out-button');
        if (elem) {        
            elem.addEventListener('click', async () => {
                UpdateConsent();
            })
        }
    }
}

// Update consent based on opt-out checkbox
const UpdateConsent = () => {
    let elem = <HTMLInputElement> document.getElementById('opt-out-choice');
    for (let p of Object.keys(_consents.purposes)) {
        _consents.purposes[p] = !elem.checked;
    }
    _host.changeConsent(_consents);    
    console.log('Update consent fired');
}
```

- Register plugin with Ketch Smart Tag
```TypeScript
// Register the custom plugin with Ketch
declare global {
    interface Window {
      semaphore: any[]
    }
}

window.semaphore = window.semaphore || [];
window.semaphore.push(['registerPlugin', OptOutPlugin]);
console.log('Cookie banner registered');
```

- Load Ketch Smart Tag and Custom Plugin
```html
<html>
    <head>
        <!--- Ketch Smart Tag --->
        <script>{(function () {var a=document.createElement("script");a.type="text/javascript",a.src="https://global.ketchcdn.com/web/v1/config/organization/web/boot.js",a.defer=a.async=!0,document.getElementsByTagName("head")[0].appendChild(a),window.semaphore=window.semaphore||[];})();}</script>

        <!--- Custom Ketch Plugin --->
        <script type='module' src='custom.ts'></script>
    </head>
    <body>
    </body>
</html>
```

## How to run example

The example uses [Parcel](https://parceljs.org), a zero configuration build tool, to bundle, build, and run the application.

- Clone the repository
```
git clone <repo>
```

- Get required packages
```
npm install
```

- Run the app
```
npm run dev
```



