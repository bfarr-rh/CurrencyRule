oc import-image registry.redhat.io/openjdk/openjdk-8-rhel8
oc new-build --name=currencyvalidation registry.redhat.io/openjdk/openjdk-8-rhel8 --binary=true
oc start-build currencyvalidation --from-dir=./target --follow
oc new-app currencyValidation


