**Note:

Configure your build file repository as below


repositories {
 
        maven {
            url "https://jitpack.io"             
            }
       
        maven {
            url "https://bitbucket.org/smartpesa/smartpesa-maven/raw/master"
            credentials {
                username "kunbiNetplus"
                password "netplusadvisory"
            }
            authentication{
                basic(BasicAuthentication)
            }
        }
        maven {
            url "https://bitbucket.org/smartpesa/maven/raw/master"
            credentials {
                username "kunbiNetplus"
                password "netplusadvisory"
            }
            authentication{
                basic(BasicAuthentication)
            }
        }
    }