import * as ketchapi from '@ketch-sdk/ketch-web-api';
import {Consent, Ketch, Plugin} from '@ketch-sdk/ketch-plugin/src/index';
import './uspapi';

import { API_VERSION, LSPA } from './globals';

// function to create uspString
// v = version (int)
// n = Notice Given (char)
// o = OptedOut (char)
// l = Lspact (char)
const createString = (notice, optedOut) => {
    return `${API_VERSION}${notice ? 'Y' : 'N'}${optedOut ? 'Y' : 'N'}${LSPA}`
}

// function to write the storage (usprivacy)
const EXDAYS = 30
const setCookie = (cvalue = "1---") => {
    let d = new Date();
    d.setTime(d.getTime() + (EXDAYS*24*60*60*1000));
    let expires = "expires="+ d.toUTCString();
    document.cookie = "usprivacy" + "=" + cvalue + ";" + expires + ";path=/";
}

// ccpaApplies is true if the ccpa regulation is associated with the jurisdiction determined in the config
let ccpaApplies = false
// @ts-ignore
const ccpa: Plugin = {
    init(_host: Ketch, config: ketchapi.Configuration){
        if (config.regulations) {
            for (const regulation of config.regulations) {
                if (regulation === 'ccpaca') {
                    ccpaApplies = true
                    return
                }
            }
        }
    },
    consentChanged: (_host: Ketch, config: ketchapi.Configuration, consent: Consent) => {
        if (!ccpaApplies) return

        const canonicalPurposes = config?.canonicalPurposes

        if (!canonicalPurposes) return

        // helpers
        const getOptOutCount = (purpose) => {
            let optOutCount = 0
            if(!consent.purposes[purpose]) {
                optOutCount += 1
            }
            return optOutCount
        }

        let isAnalyticsDisabled = false;
        let isBehavioralAdvertisingDisabled = false;
        let isDataBrokingDisabled = false;

        // consts
        let analyticsPurposeCodes = canonicalPurposes['analytics']?.purposeCodes;
        let behavioralAdvertisingPurposeCodes = canonicalPurposes['behavioral_advertising']?.purposeCodes;
        let dataBrokingPurposeCodes = canonicalPurposes['data_broking']?.purposeCodes;

        // permit logic
        analyticsPurposeCodes && analyticsPurposeCodes.map((purpose) => {
            let optOutCount = getOptOutCount(purpose)
            if(optOutCount === analyticsPurposeCodes?.length) {
                isAnalyticsDisabled = true
            }
        })
        behavioralAdvertisingPurposeCodes && behavioralAdvertisingPurposeCodes.map((purpose) => {
            let optOutCount = getOptOutCount(purpose)
            if(optOutCount === behavioralAdvertisingPurposeCodes?.length) {
                isBehavioralAdvertisingDisabled = true
            }
        })

        dataBrokingPurposeCodes && dataBrokingPurposeCodes.map((purpose) => {
            let optOutCount = getOptOutCount(purpose)
            if(optOutCount === dataBrokingPurposeCodes?.length) {
                isDataBrokingDisabled = true
            }
        })

        if(isAnalyticsDisabled || isBehavioralAdvertisingDisabled || isDataBrokingDisabled){
            const s = createString(true, true)
            setCookie(s)
        } else {
            const s = createString(true, false)
            setCookie(s)
        }
    }
}

// @ts-ignore
window.semaphore.push(['registerPlugin', ccpa])
