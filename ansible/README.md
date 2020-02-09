```bash
. ~/.pyenv/versions/venv/bin/activate
ansible-galaxy install elastic.elasticsearch,7.5.2
ansible-galaxy install jdauphant.nginx
ansible-galaxy install geerlingguy.certbot
ansible-galaxy install git+https://github.com/nickhammond/ansible-logrotate
vagrant up
```

Files to provide in folder templates: 
 - .ssh/id_rsa
 - .ssh/id_rsa.pub
 - .htpasswd
