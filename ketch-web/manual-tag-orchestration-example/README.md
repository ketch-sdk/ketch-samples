# How to add manual tag orchestration to your website

There are times when you cannot automate the firing of your tags. When those times arise, there are still ways you can gate the firing of these tags based of your visitors consent albeit a little more manual.

## Add Ketch Smart Tag to page
To start, add the Ketch Smart Tag for your property as high up in the head of the page as you can. The Ketch Smart Tag can be obtained from the corresponding property in the `Properties` page by clicking `Export Code`.
```HTML
<html>
    <head>
        <script>!function(){window.semaphore=window.semaphore||[],window.ketch=function(){window.semaphore.push(arguments)};var e=new URLSearchParams(document.location.search),o=e.has("property")?e.get("property"):"property_code",n=document.createElement("script");n.type="text/javascript",n.src="https://global.ketchcdn.com/web/v2/config/organitzation_code/".concat(o,"/boot.js"),n.defer=n.async=!0,document.getElementsByTagName("head")[0].appendChild(n)}();</script>
        ...
    </head>
    <body>
        ...
    </body>
</html>
```

## Add manual orchestration script to page
With the Ketch Smart Tag added to the page, it is time to add the manual orchestration logic. This can be done by adding the following logic to a new JavaScript file, an already existing JavaScript file, or by placing it within a script tag on your pages.

```JavaScript
For this example, we will be creating a new JavaScript file, `manual_orchestration.js`, and adding it to the head of our page below the Ketch Smart Tag.

```HTML
<html>
    <head>
        ...
        <!-- Add manual tag orchestration logic -->
        <script type="text/javascript" src="./manual_orchestration.js"></script>
        ...
    </head>
    <body>
        ...
    </body>
</html>
```
### Add manual orchestration logic to JavaScript file
- Create a new JavaScript file named `manual_orchestration.js`
```
    touch manual_orchestration.js
```

Inside `manual_orchestration.js`, add an an event listener to the `onConsent` event. This event is called whenever consent is loaded on to the page or updated by a visitor. The argument passed to the listener contains a `purposes` object.  The `purposes` object contains all the purposes defined within the Ketch Platform for your organization and the end-users consent preferences.

#### Purposes object
```json
{
    purposes: {
        essential_services: true,
        analytics: true,
        marketing: false
    }
}
```

Additionally, we get all the keys on the `purposes` object and pass them along with the `purposes` object to the `loadTagsOnPage` function.

```javascript
window.semaphore.push(['onConsent', function(c) { 
    var puporsesKeys = Object.keys(c.purposes);
    loadTagsOnPage(c.purposes, puporsesKeys);
}])
```

### Add logic to load purpose specific tags on page
Inside the `loadTagsOnPage` function, loop though the `purposes` object looking at the value for each key to determine if the function to load tags associated to that purpose should be loaded on to the page.

```javascript
function loadTagsOnPage(purposes, purposesKeys) {
    purposesKeys.forEach(key => {
        switch(key) {
            case 'essential_services':
                // Load essential services tags if visitor has given consent
                if (purposes[key] === true) {
                    loadEssentialServicesTags();
                }
                break;
            ...
        }
    });
}
```

### Loading tags on the page
Inside the function to load tags, 
- check as to whether or not the tag had been previously loaded, so as not to load the tag more than once on the page
```javascript
    if (document.getElementById('essential-script')) return;
```
- create a new script element, giving it a unique `id` to use in the previous check, and set its type to `text/javascript`
```javascript
    var essentialScript = document.createElement('script');
    essentialScript.id = 'essential-script';
    essentialScript.type = 'text/javascript';
``` 

There are 2 ways to finish the script element depending on how it was provided by the provider
1. add the source location of the JavaScript to be loaded on to the page
```javascript
    essentialScript.src = 'https://domain/filename.js'
```
2. add the JavaScript code to the tag using a text node element
```javascript
    var inlineScript = document.createTextNode(`
    (function () { 
        //<JavaScript code goes here>
    })();
    `);
```
- finally, add the script element to the `head` of the page
```javascript
    document.head.appendChild(essentialScript);
```

#### Full tag loading function
```javascript
function loadEssentialServicesTags() {
    
    if (document.getElementById('essential-script')) return;

    var essentialScript = document.createElement('script');
    essentialScript.id = 'essential-script';
    essentialScript.type = 'text/javascript';
    
    var inlineScript = document.createTextNode(`
    (function () { 
        alert('Analytics tags loaded');
    })();
    `);

    essentialScript.appendChild(inlineScript);

    document.head.appendChild(essentialScript);
}
```

## How to run the example
The example in this repository demonstrates how to manually add tags to your website based on the end-users consent. 

To run the example,
- Clone the repository
```
    git sparse-checkout set ketch-web/manual-tag-orchestration-example
```
> _Note: To configure `sparse-checkout` for the examples repository, see the main [README](../../../)_

- Open the `manual-tag-orchestration-example` directory in your terminal.
```
    cd manual-tag-orchestration-example
```

- Install required dependencies.
```
    npm install
```

- Update the Ketch Smart Tag in `Index.html` with your organization code and property code.
```
   <script>!function(){window.semaphore=window.semaphore||[],window.ketch=function(){window.semaphore.push(arguments)};var e=new URLSearchParams(document.location.search),o=e.has("property")?e.get("property"):"property_code",n=document.createElement("script");n.type="text/javascript",n.src="https://global.ketchcdn.com/web/v2/config/organization_code/".concat(o,"/boot.js"),n.defer=n.async=!0,document.getElementsByTagName("head")[0].appendChild(n)}();</script>
    ...
```

- Update the keys in the `loadTagsOnPage` function `manual_orchestration.js` file to match the purpose codes for your organization.
```javascript
function loadTagsOnPage(purposes, purposesKeys) {
    purposesKeys.forEach(key => {
        switch(key) {
            case 'essential_services':
                ...
                break;
            case 'analytics':
                ...
                break;
            case 'marketing':
                ...
                break;
        }
    });
}
```

- Run the example.
```
    npm app.js
```

- Go to the `http://localhost:3000/` URL in your browser of choice.

- The example will load the tags, executing window alerts, based on the end-users consent.