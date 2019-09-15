logLevel := Level.Warn

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.2")
addSbtPlugin("com.lucidchart"   % "sbt-scalafmt"        % "1.15")
addSbtPlugin("com.thesamet"     % "sbt-protoc"          % "0.99.12")

libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin" % "0.6.6"
