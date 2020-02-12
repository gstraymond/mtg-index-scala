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

# Fix to apply
https://github.com/nickjj/ansible-iptables/pull/2/files

# Provision
```bash
ansible-playbook playbook.yml -i hosts.ini
```

# Certbox

```bash
sudo certbox --nginx
```
