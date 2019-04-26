**Note:


In the build.gradle repositories please use this



repositories {
 
        maven { url "https://jitpack.io" }
                maven {
                    url "https://bitbucket.org/smartpesa/maven/raw/master"
                    credentials {
                        username "kunbiNetplus"
                        password "netplusadvisory"
                    }
                    authentication {
                        basic(BasicAuthentication)
                    }
                }
    }