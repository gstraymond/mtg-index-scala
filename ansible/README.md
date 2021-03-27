# Install
```bash
. ~/.pyenv/versions/venv/bin/activate
ansible-galaxy install -r requirements.yml
vagrant up
```

# Files to provide in folder templates:
 - `.ssh/id_rsa`
 - `.ssh/id_rsa.pub`
 - `.htpasswd`

# Local install

Enable `network.host: 0.0.0.0` in elasticsearch.yml
Disable iptable

# Fix to apply
https://github.com/nickjj/ansible-iptables/pull/2/files

# DNS

Set DNS search.mtg-search.com and report.mtg-search.com to the VPS, see steps in playbook.yml

# Provision
```bash
ansible-playbook playbook.yml -i hosts.ini
```

# Release

Bump version, `./gradlew distZip` and create release on github 