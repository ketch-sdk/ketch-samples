import { Plugin } from '@ketch-sdk/ketch-plugin/src/index'
import { CmpApi } from '@iabtcf/cmpapi';
import { TCModel, TCString, GVL } from '@iabtcf/core';

const cmpId = 2
const cmpVersion = 1
GVL.baseUrl = 'https://raw.githubusercontent.com/maksimiliani/ketch/main/json/';
const gvl = new GVL()
const tcModel = new TCModel(gvl);
tcModel.cmpId = cmpId
tcModel.cmpVersion = cmpVersion
tcModel.supportOOB = false
tcModel.isServiceSpecific = true
const cmpApi = new CmpApi(cmpId, cmpVersion, true);

let encodedString = ''

// gdprApplies is true if the gdpr regulation is associated with the jurisdiction determined in the config
let gdprApplies = false

// consentEncoded is true once consent is first retrieved
let consentEncoded = false

// experienceActionTaken is true if experience is showing, if it will not show, or if it is closed
let experienceActionTaken = false

// experienceShowing is false once the experience will not show or if it is closed
let experienceShowing = true

const tcf: Plugin = {
    init(_host, config){
        if (config.regulations) {
            for (const regulation of config.regulations) {
                if (regulation === 'gdpreu') {
                    gdprApplies = true
                    return
                }
            }
        }
        cmpApi.update(null);
    },
    consentChanged(_host, config, consent){
        if (!gdprApplies) {
            return
        }

        const purposeConsents: number[] = []
        const purposeLegitimateInterests: number[] = []
        const specialFeatureOptins: number[] = []
        const vendorConsents: number[] = []
        const vendorLegitimateInterests: number[] = []

        if (config.purposes) {
            for (const purpose of config.purposes) {
                if (!purpose.tcfType || !purpose.tcfID || !consent.purposes[purpose.code] || isNaN(Number(purpose.tcfID))) {
                    continue
                }
                if (purpose.tcfType === 'purpose') {
                    if (purpose.legalBasisCode === 'consent_optin') {
                        purposeConsents.push(Number(purpose.tcfID))
                        purposeLegitimateInterests.push(Number(purpose.tcfID))
                    }
                    if (purpose.legalBasisCode === 'legitimateinterest_objectable') {
                        purposeLegitimateInterests.push(Number(purpose.tcfID))
                    }
                }
                if (purpose.tcfType === 'specialFeature') {
                    specialFeatureOptins.push(Number(purpose.tcfID))
                }
            }
        }

        tcModel.purposeConsents.set(purposeConsents)
        tcModel.purposeLegitimateInterests.set(purposeLegitimateInterests)
        tcModel.specialFeatureOptins.set(specialFeatureOptins)

        let optOutVendors: { [key: string]: boolean } = {};
        if (config.vendors) {
            if (consent.vendors) {
                for (const vendor of consent.vendors) {
                    optOutVendors[vendor] = true
                }
            }

            for (const vendor of config.vendors) {
                if (optOutVendors[vendor.id] || isNaN(Number(vendor.id))) {
                    continue
                }

                vendorConsents.push(Number(vendor.id))
                vendorLegitimateInterests.push(Number(vendor.id))
            }
        }

        tcModel.vendorConsents.set(vendorConsents)
        tcModel.vendorLegitimateInterests.set(vendorLegitimateInterests)

        encodedString = TCString.encode(tcModel)
        consentEncoded = true

        // set tcString if experience showing or is hidden, otherwise wait for experience action
        if (experienceActionTaken) {
            cmpApi.update(encodedString, experienceShowing)
        }
    },
    willShowExperience(_host, _config){
        if (!gdprApplies) {
            return
        }

        // set tcString if already encoded
        experienceActionTaken = true
        if (consentEncoded) {
            cmpApi.update(encodedString, true)
        }
    },
    experienceHidden(_host, _config, _reason){
        if (!gdprApplies) {
            return
        }

        // set tcString if already encoded
        experienceActionTaken = true
        experienceShowing = false
        if (consentEncoded) {
            cmpApi.update(encodedString, false)
        }
    },
}

// @ts-ignore
window.semaphore.push(['registerPlugin', tcf])