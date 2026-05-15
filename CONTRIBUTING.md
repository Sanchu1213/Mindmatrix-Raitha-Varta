# Contributing to Raitha Varta

First off, thank you for considering contributing to Raitha Varta! It's people like you that make this tool a great resource for Karnataka's farmers.

## Where do I go from here?

If you've noticed a bug or have a feature request, make sure to check if there's already an issue for it. If not, open a new issue!

## Fork & create a branch

If this is something you think you can fix, then fork Raitha Varta and create a branch with a descriptive name.

## Implement your fix or feature

At this point, you're ready to make your changes. Feel free to ask for help if you need it.
Please follow the Kotlin coding conventions and use the established architecture (MVVM + Jetpack Compose).

## Make a Pull Request

At this point, you should switch back to your master branch and make sure it's up to date with Raitha Varta's master branch:

```bash
git remote add upstream https://github.com/Chethans3413/Raitha-Varta-Agriculture-.git
git checkout master
git pull upstream master
```

Then update your feature branch from your local copy of master, and push it!

```bash
git checkout feature/your-feature
git rebase master
git push --set-upstream origin feature/your-feature
```

Finally, go to GitHub and make a Pull Request.
