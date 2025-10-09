#!/bin/sh

gpg --batch --yes --decrypt --passphrase "$GPG_PASSPHRASE" --output src/main/resources/firebase.json src/main/resources/firebase.json.gpg

if [ -f src/main/resources/firebase.json ]; then
  echo "Decryption Success!"
fi
