function create_banner() {
    var container = document.createElement('div');
    var title = document.createElement('h2');
    title.text = 'Your Privacy';
    var text = document.createElement('p');
    text.text = "Welcome to Castle Grayskull! We’re glad you’re here and want you to know that we respect your privacy and your right to control how we collect, use, and share your personal data. Please read our privacy policy to learn about our privacy practices."
    var  button = document.createElement('button');
    button.text = 'Accept All'

    container.appendChild(title);
    container.appendChild(text);
    container.appendChild(button);

    return container;
}

var custom_plugin = {
    'init': function(host) {
        console.log('custom plugin initialized');
        
    },
    'showConsentExperience': function(host, config, consent) {
        console.log('show custom experience');
        const cb = document.getElementById('consent-banner');
        cb.appendChild(create_banner());
    },
    'showPreferenceExperience': function() {
        console.log('show pref experience');
    },
    'willShowExperience': function(host) {
        console.log('will show experience');
        console.log(host);
    }
}

window.semaphore = window.semaphore || [];
window.semaphore.push(['registerPlugin', custom_plugin]);