var _host;
var _purposes;

function accept_all_button_clicked() {
    console.log('accept all clicked');
    update_consent(true);
    var container = document.getElementById('consent-banner')
    container.style.display = 'none';
}

function create_banner() {
    var container = document.createElement('div');
    container.style.background = 'lightgray';
    container.style.width = '100%';
    container.style.height = 'auto';
    container.style.display = 'block';
    container.style.padding = '1em 0.5em 1em 0.5em';
    container.style.marginBottom = '0.5em';

    var title = document.createElement('div');
    title.innerText = 'Your Privacy';
    title.style.fontWeight = 'bold';
    title.style.fontSize = 'x-large';

    var text = document.createElement('p');
    text.innerText = "Welcome to Castle Grayskull! We're glad you're here and want you to know that we respect your privacy and your right to control how we collect, use, and share your personal data. Please read our privacy policy to learn about our privacy practices."
    
    var  button = document.createElement('button');
    button.innerText = 'Accept All'
    button.onclick = accept_all_button_clicked;

    container.appendChild(title);
    container.appendChild(text);
    container.appendChild(button);

    return container;
}

var custom_plugin = {
    'init': function(host) {
        console.log('custom plugin initialized');
        _host = host;
    },
    'showConsentExperience': function(host, config, consent) {
        console.log('show custom experience');
        const cb = document.getElementById('consent-banner');
        purposes = config.purposes;
        console.log(purposes);
        cb.appendChild(create_banner());
    }
}

function update_consent(consent_value) {
    var _consents = {
        'purposes':{}
    }
    for (var p of Object.keys(purposes)) {
        _consents.purposes[purposes[p].code]= consent_value;
    }
    console.log(_consents);
    _host.changeConsent(_consents);    
    console.log('Update consent fired');
}

window.semaphore = window.semaphore || [];
window.semaphore.push(['registerPlugin', custom_plugin]);