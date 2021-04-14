require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-helpcrunch"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-helpcrunch
                   DESC
  s.homepage     = "https://github.com/KyteApp/react-native-helpcrunch"
  s.license      = "MIT"
  s.authors      = { "Ítalo Menezes" => "italo@kyte.com.br" }
  s.platforms    = { :ios => "10.0" }
  s.source       = { :git => "https://github.com/KyteApp/react-native-helpcrunch.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
  s.dependency "HelpCrunchSDK", "~> 3.2.4"
end

