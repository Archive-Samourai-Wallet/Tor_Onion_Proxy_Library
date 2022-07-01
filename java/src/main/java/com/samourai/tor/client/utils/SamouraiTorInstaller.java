// TODO https://github.com/thaliproject/Tor_Onion_Proxy_Library/pull/127
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.samourai.tor.client.utils;

import com.msopentech.thali.toronionproxy.FileUtilities;
import com.msopentech.thali.toronionproxy.OsData;
import com.msopentech.thali.toronionproxy.TorConfig;
import com.msopentech.thali.toronionproxy.TorInstaller;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SamouraiTorInstaller extends TorInstaller {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final TorConfig config;
  private boolean useExecutableFromZip;
  private File installDir
          ;

  public SamouraiTorInstaller(String torDir, Optional<File> torExecutable, int fileCreationTimeout)
      throws Exception {
    this.config = computeTorConfig(torDir, torExecutable, fileCreationTimeout);
    this.useExecutableFromZip = !torExecutable.isPresent();
  }

  private TorConfig computeTorConfig(
      String dirName, Optional<File> torExecutable, int fileCreationTimeout) throws Exception {
    installDir = Files.createTempDirectory(dirName).toFile();
    installDir.deleteOnExit(); // not always working so we use onShutdown()

    TorConfig.Builder torConfigBuilder =
        new TorConfig.Builder(installDir, installDir).homeDir(installDir);

    if (torExecutable.isPresent()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(
            "configuring Tor for external executable: " + torExecutable.get().getAbsolutePath());
      }
      // use existing local Tor instead of embedded one
      torConfigBuilder.torExecutable(torExecutable.get());
    }
    torConfigBuilder.fileCreationTimeout(fileCreationTimeout);

    TorConfig torConfig = torConfigBuilder.build();
    return torConfig;
  }

  private static String getPathToTorExecutable() {
    String path = "native/";
    switch (OsData.getOsType()) {
      case WINDOWS:
        return path + "windows/x86/";
      case MAC:
        return path + "osx/x64/";
      case LINUX_32:
        return path + "linux/x86/";
      case LINUX_64:
        return path + "linux/x64/";
      default:
        throw new RuntimeException("We don't support Tor on this OS");
    }
  }

  public void setup() throws IOException {
    LOG.debug("Setting up tor");
    LOG.debug("Installing resources: geoip=" + this.config.getGeoIpFile().getAbsolutePath());
    FileUtilities.cleanInstallOneFile(
        this.getAssetOrResourceByName("geoip"), this.config.getGeoIpFile());
    FileUtilities.cleanInstallOneFile(
        this.getAssetOrResourceByName("geoip6"), this.config.getGeoIpv6File());

    if (useExecutableFromZip) {
      setupTorExecutable();
    } else {
      LOG.info(
          "Using existing Tor executable: " + this.config.getTorExecutableFile().getAbsolutePath());
    }
  }

  protected void setupTorExecutable() throws IOException {
    LOG.info("Installing Tor executable: " + this.config.getTorExecutableFile().getAbsolutePath());
    File torParent = this.config.getTorExecutableFile().getParentFile();
    File destination = torParent.exists() ? torParent : this.config.getTorExecutableFile();
    try {
      // detect runtime errors on extract (ie no permission)
      FileUtilities.extractContentFromZip(
          destination, this.getAssetOrResourceByName(getPathToTorExecutable() + "tor.zip"));
      FileUtilities.setPerms(this.config.getTorExecutableFile());

      // detect runtime errors on Tor executable (ie "error while loading shared libraries...")
      SamouraiTorUtils.exec(this.config.getTorExecutableFile().getAbsolutePath() + " --help");
    } catch (Exception e) {
      throw new IOException(
          "setupTorExecutable failed: "
              + e.getMessage()
              + ", destination="
              + destination.getAbsolutePath()
              + ", executable="
              + this.config.getTorExecutableFile().getAbsolutePath());
    }
  }

  public void updateTorConfigCustom(String content) throws IOException, TimeoutException {
    PrintWriter printWriter = null;

    try {
      LOG.debug("Updating torrc file; torrc =" + this.config.getTorrcFile().getAbsolutePath());
      printWriter =
          new PrintWriter(new BufferedWriter(new FileWriter(this.config.getTorrcFile(), true)));
      printWriter.println(
          "PidFile " + (new File(this.config.getDataDir(), "pid")).getAbsolutePath());
      printWriter.print(content);
    } finally {
      if (printWriter != null) {
        printWriter.close();
      }
    }
  }

  public InputStream openBridgesStream() throws IOException {
    throw new UnsupportedOperationException();
  }

  public TorConfig getConfig() {
    return config;
  }

  public void clear() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("clearing Tor directory: " + installDir.getAbsolutePath());
    }
    SamouraiTorUtils.deleteRecursively(installDir);
  }
}
