require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
    s.name           = "react-native-ucloud-rtc"
    s.version        = package["version"]
    s.summary        = package["description"]
    s.homepage       = package['homepage']
    s.license        = package['license']
    s.authors        = package["authors"]
    s.platform       = :ios, "8.0"

    s.source         = { :git => package["repository"]["url"] }
    s.source_files   = 'ios/videoView/*.{h,m}'
    s.vendored_frameworks = 'ios/UCloudRtcSdk_ios.framework'
    s.dependency 'React'
end
