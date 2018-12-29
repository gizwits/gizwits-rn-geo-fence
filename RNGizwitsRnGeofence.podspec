
Pod::Spec.new do |s|
  s.name         = "RNGizwitsRnGeofence"
  s.version      = "1.0.0"
  s.summary      = "RNGizwitsRnGeofence"
  s.description  = <<-DESC
                  RNGizwitsRnGeofence
                   DESC
  s.homepage     = ""
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.homepage     = "https://github.com/gizwits/gizwits-rn-geo-fence"
  s.source       = { :git => "https://github.com/gizwits/gizwits-rn-geo-fence.git", :tag => "master" }
  s.source_files  = "ios/**/*.{h,m}"
  s.resources = "ios/**/*.bundle", "ios/**/*.xib", "ios/**/*.storyboard"
  s.requires_arc = true

  s.dependency "React"
  #s.dependency "others"

end

  
