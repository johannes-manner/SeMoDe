#!/bin/bash

echo "Encrypt/decrypt ansible-vault"
ACTION="$1"
ENVIRONMENT="$2"
echo "Action is $1"
echo "Environment is $2"

vault() {
    for filename in "$2"; do
        if [[ "$1" = "encrypt" ]]
        then
            echo "Encrypt $2"
            ansible-vault encrypt --encrypt-vault-id "$3" "$filename"
        elif [[ "$1" = "decrypt" ]]
        then
            echo "Decrypt $2"
            ansible-vault decrypt "$filename"
        else
            echo "Arguments missing, encrypt or decrypt expected!"
        fi
    done
}

vault "$ACTION" group_vars/*.yml "$ENVIRONMENT"
vault "$ACTION" host_vars/*.yml "$ENVIRONMENT"
