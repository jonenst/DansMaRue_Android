

################################################################################
# compiler et signer co-cité en ligne de commande

Installer le android sdk dans le repertoire ${android-sdk} (au choix)
http://developer.android.com/sdk/index.html

Le mettre a jour 
${android-sdk}/tools/android.sh update sdk --no-ui

Mettre la cle dans ${key-android-dir} (au choix)

PROBLEME si maps.jar est dans libs/ => le apk s'éxecute mais plante rapidement, si il est dans ${android-sdk}/add-ons/ la tache ANT de compil ne se fait pas.
SOLUTION ${android-sdk}tools\ant\build.xml, dans la tache "-compile", ligne 682 ajouter <pathelement location="d:\\Android\\android-sdk-windows\\add-ons\\addon-google_apis-google_inc_-10\\libs\\maps.jar"/>

# Lancer la commande suivante depuis Continuum
ant clean release -Dsdk.dir=${android-sdk} -Dkey.store=${key-android-dir}/keystore -Dkey.alias=thibaut -Dkey.store.password=${password} -Dkey.alias.password=${password}
mv bin/*-release.apk ${apk-deploy-dir}
ant clean -Dsdk.dir=${android-sdk}

# windows 
ant clean release -Dsdk.dir=d:\\Android\\android-sdk-windows -Dkey.store=D:\\tmp\\android\\keystore -Dkey.alias=thibaut -Dkey.store.password=${password} -Dkey.alias.password=${password}

# deploiement
adb uninstall fr.paris.android.signalement && adb -d install "bin\\Alerte voirie-release.apk"
