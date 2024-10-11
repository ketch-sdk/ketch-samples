# Manual Orchestration Demo

This demonstrates how to conditionally execute `<script>` and `<iframe>` tags based on consent.

## Running this demo

To run this example, start a simple web server and browse to the webpage as follows:

```shell
npx http-server
```

Now, open [`http://localhost:8080/ketch-web/manual-tag-orchestration-example/manual_orchestration.html`](http://localhost:8080/ketch-web/manual-tag-orchestration-example/manual_orchestration.html) in a web browser.

> Note: Make sure to use `localhost:8080`, not `127.0.0.1:8080` to avoid iframe related CORS issues that only occur when running locally.

Based on consent state, you should see different scripts and iframes execute from [manual_orchestration.html](./manual_orchestration.html). Try hitting "Show Consent" and switching between opt-in and opt-out!

The purposes which cause a `<script>` or `<iframe>` tags to execute or not are configured in the `ketch-purposes` attribute for an element. For example, if `ketch_purposes="data_broking"` then a script tag will only fire if consent is given for the purpose with code = `data_broking`.

## Configuring your own manual orchestration

For more details and to configure manual orchestration, please see our [documentation](https://docs.ketch.com/ketch/docs/manual-tag-orchestration-with-ketch-tag).
