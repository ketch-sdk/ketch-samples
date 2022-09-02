//
//  ViewController.swift
//  iOS Ketch Pref Center using Storyboard
//
//  Created by Ryan Overton on 8/26/22.
//

import UIKit

class ViewController: UIViewController {

    private let button: UIButton = {
       let button = UIButton()
        button.setTitle("Preference Center", for: .normal)
        button.backgroundColor = .link
        button.setTitleColor(.white, for: .normal)
        return button
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.addSubview(button)
        button.addTarget(self, action: #selector(didTapButton), for: .touchUpInside)
        button.frame = CGRect(x: 0, y: 0, width: 220, height: 50)
        button.center = view.center
    }
    
    @objc private func didTapButton() {
        
        // Get URL for index.html page
        guard let url = Bundle.main.url(forResource: "index", withExtension: "html") else {
            return
        }
        
        // Add identities to URL
        let finalUrl = url.appending("visitorId", value: "ryan@test.com")
        
        // Create instance of WebViewController
        let vc = WebViewViewController(url: finalUrl, title: "Preference Center")
        
        // Navigate to Preference Center
        let navVC = UINavigationController(rootViewController: vc)
        present(navVC, animated: true)
    }
}

extension URL {

    func appending(_ queryItem: String, value: String?) -> URL {

        guard var urlComponents = URLComponents(string: absoluteString) else { return absoluteURL }

        // Create array of existing query items
        var queryItems: [URLQueryItem] = urlComponents.queryItems ??  []

        // Create query item
        let queryItem = URLQueryItem(name: queryItem, value: value)

        // Append the new query item in the existing query items array
        queryItems.append(queryItem)

        // Append updated query items array in the url component object
        urlComponents.queryItems = queryItems

        // Returns the url from new url components
        return urlComponents.url!
    }
}

