//
//  ContentView.swift
//  KetchSDK
//

import SwiftUI
import KetchSDK

struct ContentView: View {
    @ObservedObject var ketchUI: KetchUI
    
    let organizationCode = "ketch_samples"
    let propertyCode = "ios"
    /// When url is empty string the default url will be https://global.ketchcdn.com/web/v3
    let url = ""
    
    init() {
        let ketch = KetchSDK.create(
            organizationCode: organizationCode,
            propertyCode: propertyCode,
            environmentCode: "production",
            identities: [
                Ketch.Identity(key: "idfa", value: "00000000-0000-0000-0000-000000000000")
            ]
        )
        
        ketchUI = KetchUI(
            ketch: ketch,
            experienceOptions: [
                .forceExperience(.consent)
            ]
        )
    }
    
    @State var selectedExperienceToShow: KetchUI.ExperienceOption.ExperienceToShow = .consent
    @State var selectedTab: KetchUI.ExperienceOption.PreferencesTab?
    @State var lang = "EN"
    @State var jurisdiction = "default"
    @State var region = "US"
    @State var tabsExpanded = false
    @State var selectedTabs = KetchUI.ExperienceOption.PreferencesTab.allCases
    
    @ViewBuilder
    private func checkbox(_ value: Binding<Bool>) -> some View {
        Button {
            value.wrappedValue.toggle()
        } label: {
            Image(systemName: value.wrappedValue ? "circle.fill" : "circle")
        }
    }
    
    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(alignment: .leading) {
                Text("Experience:")
                Picker("Experience", selection: $selectedExperienceToShow) {
                    ForEach([KetchUI.ExperienceOption.ExperienceToShow.consent, .preferences], id: \.self) {
                        Text($0.name)
                    }
                }
                .pickerStyle(.segmented)
                
                if selectedExperienceToShow == .preferences {
                    Text("Tabs:")
                    
                    LazyVGrid(columns: [
                        GridItem(.flexible()),
                        GridItem(.flexible())
                    ]) {
                        ForEach(KetchUI.ExperienceOption.PreferencesTab.allCases, id: \.self) { tab in
                            prefTabCheckMarkView(tab: tab)
                        }
                    }
                    
                    if !selectedTabs.isEmpty {
                        HStack {
                            Text("Active tab:")
                            
                            Picker("Active tab:", selection: $selectedTab) {
                                Text("none").tag(nil as KetchUI.ExperienceOption.PreferencesTab?)
                                
                                ForEach(selectedTabs, id: \.self) { tab in
                                    Text(tab.rawValue.replacingOccurrences(of: "Tab", with: "")).tag(tab as KetchUI.ExperienceOption.PreferencesTab?)
                                }
                            }
                            .pickerStyle(.menu)
                        }
                    }
                }
                
                Text("Language:")
                Picker("Language", selection: $lang) {
                    ForEach(["EN", "FR"], id: \.self) {
                        Text($0)
                    }
                }
                .pickerStyle(.segmented)
                
                Text("Jurisdiction:")
                Picker("Jurisdiction", selection: $jurisdiction) {
                    ForEach(["default", "gdpr"], id: \.self) {
                        Text($0)
                    }
                }
                .pickerStyle(.segmented)
                
                
                Text("Region:")
                Picker("Region", selection: $region) {
                    ForEach(["US", "FR", "GB"], id: \.self) {
                        Text($0)
                    }
                }
                .pickerStyle(.segmented)
                
                Spacer()
                
                HStack {
                    Spacer()
                    
                    Button("Show") {
                        var params: [KetchUI.ExperienceOption?] = [
                            .region(code: region),
                            .language(code: lang),
                            .forceExperience(selectedExperienceToShow),
                            .jurisdiction(code: jurisdiction)
                        ]
                        
                        if !selectedTabs.isEmpty && selectedExperienceToShow == .preferences {
                            let selectedTabsNames = selectedTabs.compactMap { $0.rawValue }
                            params.append(.preferencesTabs(selectedTabsNames.joined(separator: ",")))
                            
                            if let selectedTab, selectedTabs.contains(selectedTab) {
                                params.append(.preferencesTab(selectedTab))
                            }
                        }
                        
                        if !url.isEmpty {
                            params.append(.ketchURL(url))
                        }
                        
                        ketchUI.reload(with: params.compactMap{$0})
                    }
                    .font(.system(.title))
                    
                    Spacer()
                }
                
                Spacer()
                
                Button("Log local privacy strings") {
                    showPrivacyStrings()
                }
                .padding(.bottom, 8)
                
                Text("OrgCode: \(organizationCode)")
                Text("PropertyCode: \(propertyCode)")
                Text("URL: \(url.isEmpty ? "https://global.ketchcdn.com/web/v3" : url)")
            }
            .padding()
        }
        .background(.white)
        .ketchView(model: $ketchUI.webPresentationItem)
    }
    
    private func showPrivacyStrings() {
        // fir some reson preview is not working when this strings are all in one array
        let keys = ["IABTCF_CmpSdkID",
                    "IABTCF_CmpSdkVersion",
                    "IABTCF_PolicyVersion",
                    "IABTCF_gdprApplies",
                    "IABTCF_PublisherCC",
                    "IABTCF_PurposeOneTreatment",
                    "IABTCF_UseNonStandardTexts",
                    "IABTCF_TCString",
                    "IABTCF_VendorConsents"]
        
        let keys2 = ["IABTCF_VendorLegitimateInterests",
                     "IABTCF_PurposeConsents",
                     "IABTCF_PurposeLegitimateInterests",
                     "IABTCF_SpecialFeaturesOptIns",
                     "IABTCF_PublisherConsent",
                     "IABTCF_PublisherLegitimateInterests",
                     "IABTCF_PublisherCustomPurposesConsents",
                     "IABTCF_PublisherCustomPurposesLegitimateInterests",
                     "IABUSPrivacy_String"]
        
        let keys3 = ["IABGPP_HDR_Version",
                     "IABGPP_HDR_Sections",
                     "IABGPP_HDR_GppString",
                     "IABGPP_GppSID",
                     "IABGPP_tcfeuv2_GppSID"]
        
        print("\n* ----- Begin privacy strings ---- *")
        (keys + keys2 + keys3).forEach {
            print("\($0): \(UserDefaults.standard.value(forKey: $0) ?? "")")
        }
        print("* ----- End privacy strings ---- *\n")
    }
    
    private func prefTabCheckMarkView(tab: KetchUI.ExperienceOption.PreferencesTab) -> some View {
        HStack {
            Image(systemName: selectedTabs.contains(tab) ? "checkmark.square" : "square")
            Spacer()
            Text(tab.rawValue.replacingOccurrences(of: "Tab", with: ""))
        }
        .padding(2)
        .background(.gray.opacity(0.2))
        .cornerRadius(3)
        .onTapGesture {
            if let index = selectedTabs.firstIndex(of: tab) {
                selectedTabs.remove(at: index)
            } else {
                selectedTabs.append(tab)
            }
        }
    }
}

extension KetchUI.ExperienceOption.ExperienceToShow {
    var name: String {
        switch self {
        case .consent:
            return "Consent"
        case .preferences:
            return "Preferences"
        }
    }
}

#Preview {
    ContentView()
}
