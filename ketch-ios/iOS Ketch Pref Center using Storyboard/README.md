# Using the Ketch Smart Tag within a Storyboard based Swift project

This example demonstrates how you can use the Ketch Smart Tag within a SwiftUI based iOS application to display the preferences view. It also shows how to pass in an identity and add it to the dataLayer to be picked up by the Ketch Smart Tag.

The example utilizes an HTML page embedded into the application, but can easily be substituted to display a similar page hosted on a website. To display the page in the iOS app, it utilizes the WebKit WebView from the WebKit framework.

<img src="./assets/example.gif" height=300>

## Prerequisites
- A property configured and deployed within the Ketch Platform.
- A [custom identity space configured on the deployed property](https://docs.ketch.com/hc/en-us/articles/360063594173-Managing-Properties#configuring-data-layer-setup-0-9) to read it's value from the `dataLayer`.

## Getting Started

To get going, open your SwiftUI based iOS project, or create a new one.

Next, we'll add a new file with the `Empty` template and name it `index.html`.

Inside the `index.html` file, add a standard webpage structure like below and set the `viewport` size for the page.

```html
<html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
    </head>
    <body></body>
</html>
```

We'll be adding 3 scripts to the `head` of our page to do the following:
- Add the identities passed in through the query parameters to the `dataLayer`
- Load the Ketch Smart Tag
- Create a custom plugin to show the Ketch Preference Center and respond to the closing of the Ketch Preference Center

### Add identities to the `dataLayer`
The identities will be passed to the page through the query parameters of the URL. Once we have the identities, then we need to add them to the dataLayer, creating it first if it does not already exist. Add the following script to the `head` of the `index.html` page below the `<meta>` tag.

```html
<head>
    ...
    <script>
        //get query parameters
        let params = (new URL(document.location)).searchParams;
        let visitorIdValue = params.get("visitorId");
        
        //add identities to dataLayer
        window.dataLayer = window.dataLayer || [];
        window.dataLayer.push({visitorId: visitorIdValue});
      
        // Add multiple identities
        //params.foreach((value, key) => {
        //    window.dataLayer.push({key: value});
        //}   
    </script>
    ...
</head>
```

In this example, we are expecting a `visitorId` to be passed in through the query parameters. This ID could be any identifier used to identify the user using the mobile app. It should also correspond to the identifier setup within the deployed Ketch Property on the Ketch Platform. 

## Load the Ketch Smart Tag
Next, load the Ketch Smart Tag onto the page. This script can be obtained by [exporting it from the Ketch Property page](https://docs.ketch.com/hc/en-us/articles/360062451394-Tag-Implementation).

It should look similar to the `script` below, except it will contain your organizations code and the corresponding property code.

```html
    <script>!function(){var e=document.createElement("script");e.type="text/javascript",e.src="https://global.ketchcdn.com/web/v1/config/org_code/property_code/boot.js",e.defer=e.async=!0,document.getElementsByTagName("head")[0].appendChild(e),window.semaphore=window.semaphore||[]}();</script>
```

_note: the Ketch Smart Tag script should be loaded ***AFTER*** the population of the identitied in the `dataLayer` this will ensure the Ketch Smart Tag will load the users previous consent choices. Providing a cohesive experience with your brand across their various platforms (web, mobile, etc.)_

### Create the custom plugin
Finally, we'll need to create a custom plugin to show and listen for the closing of the Ketch Preference Center.

When the Ketch Preference Center experience closes, the `experienceHidden` event is fired. The event will pass a reason for the experience being closed. The reason will either be a string containing the value `setConsent`, if a submit button was pressed, or a `MouseEvent`, indicating an exit button was pressed indicating the user did not want to save their choices.

To communicate back to the iOS application, the `script` below makes use of `WebKit Message Handlers`. 

_note: If you name your message handler something other than `iosListener` within your iOS app, ensure update the calls below, otherwise messages sent from this script will not be received in your iOS app._

```html
<html>
    <head>
        ...
        <script>
            // Show the preference center experience
            window.semaphore.push(['showPreferences'])
            
            // Custom plugin to listen for the closing of the preference center experience
            var custom_plugin = {
                'init': function(host) {
                    window.webkit.messageHandlers.iosListener.postMessage('custom plugin initialized');
                },
                'experienceHidden': function(host, config, reason) {
                    if ((reason.constructor.name === 'String' && reason === 'setConsent') || reason.constructor.name === 'MouseEvent') {
                        window.webkit.messageHandlers.iosListener.postMessage('PreferenceCenterClosed');
                    }
                }
            }
            
            // Register the custom plugin with the Ketch Smart Tag
            window.semaphore = window.semaphore || [];
            window.semaphore.push(['registerPlugin', custom_plugin]);
        </script>
        ...
    </head>
</html>
```

## Showing the HTML page in an IOS app

With the `index.html` page created, it's time to show it within an iOS application. 

To show our page, we'll utilize `WKWebView` from the WebKit Framework.

To get started, open your application in Xcode, or create a new Storyboard application.

- Add a new file to the project, template `Swift`, name it `WebViewController`.

- Import the `UIKit` and `WebKit` frameworks
```swift
import UIKit
import WebKit
```

- Add a class named `WebViewController` which inherits from `UIViewController`.
```swift
class WebViewController: UIViewController {

}
```

- Add a property to hold the page URL for the WebView and create an initializer to recieve the URL and a title for the screen in our app.
```swift
class WebViewController: UIViewController {
    private let url: URL
    
    init (url: URL, title: String) {
        self.url = url
        super.init(nibName: nil, bundle: nil)
        self.title = title
    }
    
    required init?(coder: NSCoder) {
        fatalError()
    }
}
```

- Create a function to get the configuration needed for the WebView to be able to execute JavaScript on the HTML page.
```swift
class WebViewController: UIViewControllew {
    
    ...

    func getWebViewConfig() -> WKWebViewConfiguration {
        let preferences = WKWebpagePreferences()
        preferences.allowsContentJavaScript = true
        
        let configuration = WKWebViewConfiguration()
        configuration.defaultWebpagePreferences = preferences
        
        return configuration
    }
}
```

- Override the `viewDidLoad` function, getting the WebView configuration, creating the `WKWebView` instance passing, and adding it to the current view.
```swift
class WebViewController: UIViewControllew {
    
    ...
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Get WKWEbView configuration
        let configuration = getWebViewConfig()

        // Create the WebView
        var webView = WKWebView(frame: .zero, configuration: configuration)
        
        // Add the WebView to the current view
        view.addSubview(webView)
    }
}
```

- Create a new function to set the layout for the WebView, and add the call to it inside the `viewDidLoad` function after the WebView has been added to the View.
```swift
class WebViewController: UIViewControllew {
    
    ...
    
    func configureWebViewLayout(webView: inout WKWebView) {
        let layoutGuide = view.safeAreaLayoutGuide

        webView.translatesAutoresizingMaskIntoConstraints = false
        webView.leadingAnchor.constraint(equalTo: layoutGuide.leadingAnchor).isActive = true
        webView.trailingAnchor.constraint(equalTo: layoutGuide.trailingAnchor).isActive = true
        webView.topAnchor.constraint(equalTo: layoutGuide.topAnchor).isActive = true
        webView.bottomAnchor.constraint(equalTo: layoutGuide.bottomAnchor).isActive = true
    }
    
    override func viewDidLoad() {

        ...
        
        // Add the WebView to the current view
        view.addSubview(webView)
        
        // Configure WebViews Layout
        configureWebViewLayout(webView: &webView)
    }
}
```

- Navigate the WebView to the URL received during class initialization at the end of the `viewDidLoad` function.
```swift
class WebViewController: UIViewControllew {
    
    ...
    
    override func viewDidLoad() {

        ...
        
        // Navigate to URL in the WebView
        webView.load(URLRequest(url: url))
    }
}
```

- In the ViewController where you want to add the WebView, add an event handler to show the WebView. In the example, there is a `UIButton` to launch the WebView.
```swift
class ViewController: UIViewController {

    ...
    
    @objc private func didTapButton() {

    }

    ...

}
```

- In the event handler, get the URL for the `index.html` file created earlier, create an instance of the WebViewController passing the URL and a title for the page.
```swift
class ViewController: UIViewController {

    ...
    
    @objc private func didTapButton() {
        
        guard let url = Bundle.main.url(forResource: "index", withExtension: "html") else {
            return
        }
        
        let finalUrl = url.appending("visitorId", value: "ryan@test.com")
        let vc = WebViewViewController(url: finalUrl, title: "Preference Center")
    }

    ...

}
```

- Create a `UINavigationController`, if one does not already exist, adding the `WebViewController` instance to it, and navigate to the `WebViewController`.
```swift
class ViewController: UIViewController {

    ...
    
    @objc private func didTapButton() {
        
        ...

        let navVC = UINavigationController(rootViewController: vc)
        present(navVC, animated: true)
    }

    ...

}
```

When we test our app, it will now show our HTML page, which will in turn show the Ketch Preference Center for our organization.

## Closing the View when the Preference Center Experience closes

Now that our app shows the Ketch Preference Center, the `PreferenceView` should automatically close when the user submits consent changes or closes the experience. To do this, we'll need to add a message handler to the `WebViewController` to receive the message from the HTML page to close the view.

- Back in the `WebViewController`, add an extension for the `WebViewController` class inheriting from `WKScriptMessageHandler`, and implement the required function `userContentController`.
```swift
class WebViewController: UIViewController {
    
    ...
    
    extension WebViewViewController: WKScriptMessageHandler {
    // Capture postMessage() calls inside loaded JavaScript from the webpage. Note that a Boolean
    // will be parsed as a 0 for false and 1 for true in the message's body. See WebKit documentation:
    // https://developer.apple.com/documentation/webkit/wkscriptmessage/1417901-body.
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
    }
}}
```

- Next check the message received to see if it indicates the user has closed the Ketch Preference Center. In our example, the string we expect to receive is `PreferenceCenterClosed` which corresponds to what we have setup in our custom plugin on our HTML page. If the expected string is received, dismiss the `WebViewController`
```swift
class WebViewController: UIViewController {
    
    ...

 }

extension WebViewViewController: WKScriptMessageHandler {
    // Capture postMessage() calls inside loaded JavaScript from the webpage. Note that a Boolean
    // will be parsed as a 0 for false and 1 for true in the message's body. See WebKit documentation:
    // https://developer.apple.com/documentation/webkit/wkscriptmessage/1417901-body.
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        if (message.body as? String == "PreferenceCenterClosed") {
                dismiss(animated: true, completion: nil)
        }
    }
}
```

- Update the `getWebViewConfig` function to create an instance of the `userContentController`, add a message handler with the name of `iosListener`, and add it to the configuration.
```swift
class WebViewController: UIViewController {
    
    ...
    
    func getWebViewConfig() -> WKWebViewConfiguration {
        
        ...
        
        let userContentController = WKUserContentController()
        userContentController.add(self, name: "iosListener")
        
        configuration.userContentController = userContentController
        
        return configuration
    }

}
```

Now when you test your app, the page should close upon the user clicking the `Submit` button, or the `Exit` button.

## Passing identities

To ensure the customer's consent choices move with them no matter what device they use to interact with your brand, we can send identity information already present in the app to the Ketch Smart Tag running on the HTML page.

The HTML page we created earlier is already expecting and checking for identity information in query parameters of the URL. So, now all we need to do is append these to our URL.

- To begin, create an extension method to add a query parameter to an existing url. Place the extension at the bottom of the View Controller creating the WebViewController.
```swift
extension URL {

    func appending(_ queryItem: String, value: String?) -> URL {
        guard var urlComponents = URLComponents(string: absoluteString) else { return absoluteURL }
        var queryItems: [URLQueryItem] = urlComponents.queryItems ??  []
        let queryItem = URLQueryItem(name: queryItem, value: value)
        queryItems.append(queryItem)
        urlComponents.queryItems = queryItems
        return urlComponents.url!
    }
}
```

- Update the event handler showing the View Controller to append the identity to the `index.html` page url.
```swift
class ViewController: UIViewController {

    ...

    @objc private func didTapButton() {
        
        ...
        
        let finalUrl = url.appending("visitorId", value: "user@test.com")
        let vc = T1(url: finalUrl, title: "Preference Center")
        
        ...

    }
}
```

Now when you test your application and have set a consent state, you will be able to search the Ketch Audit Logs by the identity your passing through to the `dataLayer` within the HTML page.