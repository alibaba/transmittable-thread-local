# Welcome! Thank you for contributing to TransmittableThreadLocal(TTL)!

> ⚠️ This contribution guide is still in progress.

We follow the standard GitHub [fork & pull](https://help.github.com/articles/using-pull-requests/#fork--pull) approach to pull requests. Just fork the official repo, develop in a branch, and submit a PR!

You're always welcome to submit your PR straight away and start the discussion (without reading the rest of this wonderful doc, or the README.md). The goal of these notes is to make your experience contributing to TransmittableThreadLocal(TTL) as smooth and pleasant as possible. We're happy to guide you through the process once you've submitted your PR.

# The TransmittableThreadLocal(TTL) Community

Mainly use the github issue: https://github.com/alibaba/transmittable-thread-local/issues

In case of questions about the contribution process or for discussion of specific issues please visit the [alibaba/transmittable-thread-local gitter chat](https://gitter.im/alibaba/transmittable-thread-local?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge).

# Navigating around the project & codebase

## Branches summary

Depending on what you want to work on, you should target a specific branch as explained below:

* `master` – active development branch
* `incubation/xxx` branches contain big feature.
* `dev/xxx` branches contain small feature or bug fix.

## Tags

TTL uses tags to categorise issues into groups. 

Most notably many tags start with a `t:` prefix (as in `topic:`), which categorises issues in terms of which extension module they relate to. Examples are:

- [t:vertx](https://github.com/alibaba/transmittable-thread-local/labels/t%3Avertx)
- [t:netty](https://github.com/alibaba/transmittable-thread-local/labels/t%3Anetty)

Without `t:xxx` tags is related to `TTL` lib.

see [all tags here](https://github.com/alibaba/transmittable-thread-local/labels)

In general *all issues are open for anyone working on them*, however if you're new to the project and looking for an issue
that will be accepted and likely is a nice one to get started you should check out the following tags:

- [good first issue](https://github.com/alibaba/transmittable-thread-local/labels/%F0%9F%94%B0%20good%20first%20issue) - which identifies simple entry level tickets, such as improvements of documentation or tests. If you're not sure how to solve a ticket but would like to work on it feel free to ask in the issue about clarification or tips.
- [help wanted](https://github.com/alibaba/transmittable-thread-local/labels/help%20wanted) - which identifies issues that the core team will likely not have time to work on, or the issue is a nice entry level ticket. If you're not sure how to solve a ticket but would like to work on it feel free to ask in the issue about clarification or tips.
- [nice-to-have (low-priority)](https://github.com/alibaba/transmittable-thread-local/labels/nice-to-have%20%28low-prio%29) - are tasks which make sense, however are not very high priority (in face of other very high priority issues). If you see something interesting in this list, a contribution would be really wonderful!

Another group of tickets are those which start from a number. They're used to signal in what phase of development an issue is:

- [0 - new](https://github.com/alibaba/transmittable-thread-local/labels/0%20-%20new) - is assigned when a ticket is unclear on its purpose or if it is valid or not. Sometimes the additional tag `discuss` is used to mark such tickets, if they propose large scale changes and need more discussion before moving into triaged (or being closed as invalid).
- [1 - triaged](https://github.com/alibaba/transmittable-thread-local/labels/1%20-%20triaged) - roughly speaking means "this ticket makes sense". Triaged tickets are safe to pick up for contributing in terms of likeliness of a patch for it being accepted. It is not recommended to start working on a ticket that is not triaged.
- [2 - pick next](https://github.com/alibaba/transmittable-thread-local/labels/2%20-%20pick%20next) - used to mark issues which are next up in the queue to be worked on. Sometimes it's also used to mark which PRs are expected to be reviewed/merged for the next release. The tag is non-binding, and mostly used as an organisational helper.
- [3 - in progress](https://github.com/alibaba/transmittable-thread-local/labels/3%20-%20in%20progress) - means someone is working on this ticket. If you see a ticket that has the tag, however seems inactive, it could have been an omission with removing the tag, feel free to ping the ticket then if it's still being worked on.

Another group of tags indicate type of a ticket is:

- [bug](https://github.com/alibaba/transmittable-thread-local/labels/%F0%9F%90%9E%20bug) tickets indicate potential production issues. Bugs take priority in being fixed above features. The core team dedicates a number of days to working on bugs each sprint. Bugs which have **reproducers** are also great for community contributions as they're well-isolated. Sometimes we're not as lucky to have reproducers though, then a bugfix should also include a test reproducing the original error along with the fix.
- [feature](https://github.com/alibaba/transmittable-thread-local/labels/%E2%9C%A8%20feature).
- [enhancement](https://github.com/alibaba/transmittable-thread-local/labels/%F0%9F%92%AA%20enhancement).

# TransmittableThreadLocal(TTL) contributing guidelines

These guidelines are meant to be a living document that should be changed and adapted as needed.
We encourage changes that make it easier to achieve our goals in an efficient way.

## General workflow

The steps below describe how to get a patch into a main development branch (e.g. `master`). 
The steps are exactly the same for everyone involved in the project (be it core team, or first time contributor).

1. To avoid duplicated effort, it might be good to check the [issue tracker](https://github.com/alibaba/transmittable-thread-local/issues) and [existing pull requests](https://github.com/alibaba/transmittable-thread-local/pulls) for existing work.
   - If there is no ticket yet, feel free to [create one](https://github.com/alibaba/transmittable-thread-local/issues/new) to discuss the problem and the approach you want to take to solve it.
1. [Fork the project](https://github.com/alibaba/transmittable-thread-local/fork) on GitHub. You'll need to create a feature-branch for your work on your fork, as this way you'll be able to submit a pull request against the mainline.
1. Create a branch on your fork and work on the feature. For example: `git checkout -b support-fast-thread-local`
   - Please make sure to follow the general quality guidelines (specified below) when developing your patch.
   - Please write additional tests covering your feature and adjust existing ones if needed before submitting your pull request. The `validatePullRequest` task ([explained below](#the-validatepullrequest-task)) may come in handy to verify your changes are correct.
   - Use the `verifyCodeStyle` maven task to make sure your code is properly formatted and includes the proper copyright headers.
1. Once your feature is complete, prepare the commit following our [Creating Commits And Writing Commit Messages](#creating-commits-and-writing-commit-messages). For example, a good commit message would be: `Adding compression support for Manifests #42` (note the reference to the ticket it aimed to resolve).
1. If it's a new feature, or a change of behavior, document it on the [User Guide](https://github.com/alibaba/transmittable-thread-local/blob/master/README.md). If the feature was touching Scala or Java DSL, make sure to document both the Scala and Java APIs.
1. Now it's finally time to [submit the pull request](https://help.github.com/articles/using-pull-requests)!
    - Please make sure to include a reference to the issue you're solving *in the comment* for the Pull Request, as this will cause the PR to be linked properly with the Issue. Examples of good phrases for this are: "Resolves #1234" or "Refs #1234".
1. If you have not already done so, you will be asked by our CLA bot to [sign the Alibaba CLA](https://cla-assistant.io/alibaba/transmittable-thread-local) online. CLA stands for Contributor License Agreement and is a way of protecting intellectual property disputes from harming the project.
1. Now both committers and interested people will review your code. This process is to ensure the code we merge is of the best possible quality, and that no silly mistakes slip through. You're expected to follow-up these comments by adding new commits to the same branch. The commit messages of those commits can be more loose, for example: `Removed debugging using printline`, as they all will be squashed into one commit before merging into the main branch.
    - The community and team are really nice people, so don't be afraid to ask follow up questions if you didn't understand some comment, or would like clarification on how to continue with a given feature. We're here to help, so feel free to ask and discuss any kind of questions you might have during review!
1. After the review you should fix the issues as needed (pushing a new commit for new review etc.), iterating until the reviewers give their thumbs up–which is signalled usually by a comment saying `LGTM`, which means "Looks Good To Me".
1. Once everything is said and done, your pull request gets merged :tada: Your feature will be available with the next “earliest” release milestone. And of course you will be given credit for the fix in the release stats during the release's announcement. You've made it!

The TL;DR; of the above very precise workflow version is:

1. Fork TTL
2. Hack and test on your feature (on a branch)
3. Document it
4. Submit a PR
5. Sign the CLA if necessary
6. Keep polishing it until received enough LGTM
7. Profit!

## Getting started with maven

TTL is using the [maven](https://maven.apache.org/) build system.

To compile all the core modules use the `compile` command:

```bash
./mvnw compile
```

You can run tests with the `test` command:

```bash
./mvnw test
```

If you want to deploy the artifacts to your local maven repository (for example,
to use from an maven project) use the `install` command:

```bash
./mvnw install
```

## The Pull Request validation task

**TODO**

## Binary compatibility

**TODO**

## Pull request requirements

For a pull request to be considered at all it has to meet these requirements:

1. Regardless if the code introduces new features or fixes bugs or regressions, it must have comprehensive tests.
1. The code must be well documented in the TTL project's standard documentation format (see the ‘Documentation’ section below).
1. The commit messages must properly describe the changes, see further below.
1. A pull request must indicate (link to) the issue it is aimed to resolve in the description (or comments) of the PR, in order to establish a link between PR and Issue. This can be achieved by writing "Fixes #1234" or similar in PR description.
1. All projects must include TTL copyright notices.  Each project can choose between one of two approaches:

    1. All source files in the project must have a TTL copyright notice in the file header.
    1. The Notices file for the project includes the TTL copyright notice and no other files contain copyright notices.  See http://www.apache.org/legal/src-headers.html for instructions for managing this approach for copyrights.

    TTL projects uses the first choice, having copyright notices in every file header. When absent, these are added automatically during `mvn compile`.

### Additional guidelines

Some additional guidelines regarding source code are:

- Keep the code [DRY](http://programmer.97things.oreilly.com/wiki/index.php/Don%27t_Repeat_Yourself).
- Apply the [Boy Scout Rule](http://programmer.97things.oreilly.com/wiki/index.php/The_Boy_Scout_Rule) whenever you have the chance to.
- Never delete or change existing copyright notices, just add additional info.
- Do not use ``@author`` tags since it does not encourage [Collective Code Ownership](http://www.extremeprogramming.org/rules/collective.html). TODO
  - Contributors , each project should make sure that the contributors gets the credit they deserve—in a text file or page on the project website and in the release notes etc.

## Documentation

All documentation is preferred to be in Java API doc standard documentation format, which among other things allows all code in the documentation to be externalized into compiled files and imported into the documentation.

To build the documentation locally:

```
mvn -Pgen-api-doc javadoc:javadoc
```

## Creating commits and writing commit messages

Follow these guidelines when creating public commits and writing commit messages.

1. If your work spans multiple local commits (for example; if you do safe point commits while working in a feature branch or work in a branch for a long time doing merges/rebases etc.) then please do not commit it all but rewrite the history by squashing the commits into a single big commit which you write a good commit message for (like discussed in the following sections). For more info read this article: [Git Workflow](http://sandofsky.com/blog/git-workflow.html). Every commit should be able to be used in isolation, cherry picked etc.

2. The first line should be a descriptive sentence what the commit is doing, including the ticket number. It should be possible to fully understand what the commit does—but not necessarily how it does it—by just reading this single line. We follow the “imperative present tense” style for commit messages ([more info here](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html)).

   It is **not ok** to only list the ticket number, type "minor fix" or similar.
   If the commit is a small fix, then you are done. If not, go to 3.

3. Following the single line description should be a blank line followed by an enumerated list with the details of the commit.

4. You can request review by a specific team member for your commit (depending on the degree of automation we reach, the list may change over time):
    * ``Review by @gituser`` - if you want to notify someone on the team. The others can, and are encouraged to participate.

Example:

    enable Travis CI #1

    * Details 1
    * Details 2
    * Details 3

## Pull request validation workflow details

**TODO**

## Source style

Sometimes it is convenient to place 'internal' classes in their own package.
In such situations we prefer 'internal' over 'impl' as a package name.

### Java style

TTL projects uses xxx maven plugin to format Java sources.

**TODO**

PR validation includes checking that the Java sources are formatted and will fail if they are not.

# Supporting infrastructure

## Reporting security issues

If you have found an issue in an TransmittableThreadLocal(TTL) project that might have security
implications, you can report it to <https://security.alibaba.com>. We will make
sure those will get handled with priority. Thank you for your responsible
disclosure!

## Continuous integration

TransmittableThreadLocal(TTL) uses GitHub actions for Continuous Integration: https://github.com/alibaba/transmittable-thread-local/actions

## Related links

- [alibaba/transmittable-thread-local Contributor License Agreement](https://cla-assistant.io/alibaba/transmittable-thread-local)
- [the akka contribution guide](https://github.com/akka/akka/blob/master/CONTRIBUTING.md) (this contribution guide is adapted from it)
