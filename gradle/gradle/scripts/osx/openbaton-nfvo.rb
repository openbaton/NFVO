class OpenbatonNfvo < Formula
  desc "Formula of Open Baton NFVO"
  homepage "http://www.openbaton.org"
  url "https://codeload.github.com/openbaton/NFVO/legacy.tar.gz/5.0"
  # sha256 "9dcedc2035905eab7a787c887ba2040f0f1153d51b14622d1844b6af5dbcb71a"

  depends_on :java => "1.8+"
  depends_on "rabbitmq"

  def install
    system "./gradlew", "installDist"
    # Change external path with /usr/local/etc instead /etc (due to brew internal directories)
    inreplace "build/install/openbaton-nfvo/bin/openbaton-nfvo" do |s|
      s.gsub! /^DEFAULT_CONFIG_FILE_PATH=.*/, "DEFAULT_CONFIG_FILE_PATH=#{etc}/openbaton/openbaton-nfvo.properties"
      s.gsub! /APP_HOME="`pwd -P`"/, %Q(APP_HOME="#{libexec}")
    end
    # inreplace "build/install/openbaton-nfvo/bin/openbaton-nfvo", "DEFAULT_CONFIG_FILE_PATH=/etc/openbaton/openbaton-nfvo.properties", "DEFAULT_CONFIG_FILE_PATH=#{etc}/openbaton/openbaton-nfvo.properties"
    # Change application path
    # inreplace "build/install/openbaton-nfvo/bin/openbaton-nfvo", /APP_HOME="`pwd -P`"/, %Q(APP_HOME="#{libexec}")

    # Copy the openbaton-nfvo.properties in the right location
    openbaton_properties_path = etc+"openbaton"
    openbaton_properties_path.mkpath
    openbaton_properties_path.install "main/src/main/resources/application.properties"

    # Remove Windows file
    rm_f Dir["build/install/openbaton-nfvo/bin/*.bat"]

    libexec.install Dir["build/install/openbaton-nfvo/*"]
    bin.install_symlink Dir["#{libexec}/bin/openbaton-nfvo"]
  end
  test do
    system "false"
  end
end