//
//  ConsentView.swift
//  iOS Ketch Pref Center using SwiftUI
//

import SwiftUI
import WebKit

struct ConsentView: UIViewRepresentable {
    let config: ConsentConfig
    @Environment(\.presentationMode) private var presentationMode

    func makeUIView(context: Context) -> some UIView {
        config.preferencesWebView(
            onClose: {
                presentationMode.wrappedValue.dismiss()
            }
        )
    }
    
    func updateUIView(_ uiView: UIViewType, context: Context) { }
}
