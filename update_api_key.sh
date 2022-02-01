#!/bin/bash

echo "CRYPTOFINTECHX Production API Key Updater"

read -sp "Input new API Key: " apikey

echo

echo "Updating production server configs..."

echo "Updating api production servers..."

heroku config:set API_KEY=$apikey -a cryptofintechx-backend

echo "Updating dashboard production servers..."

heroku config:set API_KEY=$apikey -a cryptofintechx-dashboard
