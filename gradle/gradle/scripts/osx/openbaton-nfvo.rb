class OpenbatonNfvo < Formula
  desc "Formula of Open Baton NFVO"
  homepage "http://www.openbaton.org"
  url "https://codeload.github.com/openbaton/NFVO/legacy.tar.gz/2.2.0"
  version "2.2.0"
  # sha256 "9dcedc2035905eab7a787c887ba2040f0f1153d51b14622d1844b6af5dbcb71a"

  depends_on :java => "1.7+"
  depends_on "gradle"
  depends_on "rabbitmq"

  def install
    system "./openbaton.sh", "compile"
    system "./gradlew", "installDist"
    # Change external path with /usr/local/etc instead /etc (due to brew internal directories)
    inreplace "build/install/openbaton/bin/openbaton-nfvo", "CONFIG_FILE=/etc/openbaton/openbaton.properties", "CONFIG_FILE=#{etc}/openbaton/openbaton.properties"
    # Change application path
    inreplace "build/install/openbaton/bin/openbaton-nfvo", /APP_HOME="`pwd -P`"/, %(APP_HOME="#{libexec}")

    # Copy the openbaton.properties in the right location
    openbaton_properties_path = etc+"openbaton"
    openbaton_properties_path.mkpath
    openbaton_properties_path.install "etc/openbaton.properties"

    # Remove Windows file
    rm_f Dir["build/install/openbaton/bin/*.bat"]

    libexec.install Dir["build/install/openbaton/*"]
    bin.install_symlink Dir["#{libexec}/bin/openbaton-nfvo"]
  end
  test do
    system "false"
  end
end
