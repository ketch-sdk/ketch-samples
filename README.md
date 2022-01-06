# Welcome to the Ketch Sample Applications repository!

We're using one large monorepo to manage all our sample applications for Ketch Smart Tag, Ketch Plugin, Ketch iOS, Ketch Android, and Ketch Rest APIs. 

If you'd like to clone a single sample to run and play with, follow the steps below.

1. Create the folder where the sample app will reside and navigate into it.
```
md ketch-samples
cd ketch-samples
```

2. Initialize the git repository
```
git init
```

3. Add the remote repository to the newly intialized got repo
```
git remote add -f origin https://github.com/ketch-sdk/ketch-samples.git
```

4. Update the config file
```
git config core.sparseCheckout true
```

5. Initialize sparse-checkout
```
git sparse-checkout init
```

6. Add folders to pull during sparse-checkout
```
git sparse-checkout set ketch-plugin/ketch-custom-consent-plugin
```

7. Pull folders
```
git pull origin main
```