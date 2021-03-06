import { Consent, Ketch, Plugin } from '@ketch-sdk/ketch-plugin/src/index'

let _host: Ketch
let _consents: Consent

// Create custom plugin to handle consent changes to Ketch
const OptOutPlugin: Plugin = {
    init(host) {

        _host = host;

        // Get current consent state
        host.getConsent().then(consent => { 
            _consents = consent; 
            UpdateOptOutCheckBox(); 
        }).catch(err => console.log(err));
        
        // Add event listener to opt-out button
        const elem = document.getElementById('opt-out-button');
        if (elem) {        
            elem.addEventListener('click', () => {
                UpdateConsent().catch(err => console.log(err));
            });
        }
    }
}

// Update opt-out checkbox based on consent
const UpdateOptOutCheckBox = () => {
    const purposeKeys = Object.keys(_consents.purposes);
    const elem = <HTMLInputElement> document.getElementById('opt-out-choice');
    if (elem) {
        elem.checked = !_consents.purposes[purposeKeys[0]];
    }
}

// Update consent based on opt-out checkbox
const UpdateConsent = async () => {
    const elem = <HTMLInputElement> document.getElementById('opt-out-choice');
    for (const p of Object.keys(_consents.purposes)) {
        _consents.purposes[p] = !elem.checked;
    }
    await _host.changeConsent(_consents);    
    console.log('Update consent fired');
}



// Register the custom plugin with Ketch
declare global {
    interface Window {
      semaphore: unknown[]
    }
}

window.semaphore = window.semaphore || [];
window.semaphore.push(['registerPlugin', OptOutPlugin]);
console.log('Cookie banner registered');