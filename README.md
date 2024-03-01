# LogHound ![](https://img.shields.io/badge/License-MIT-blue)

A Tool for constructing method call chain and calculate suspicion in complicated system.
I have merged the previous work in graph construction.

# Why this Name
A neta from Bloodhound. Hope for its scanning the trace and give insight for the bug detection.

# Introduction

| Name   | Description                       |
|--------|-----------------------------------|
| AST    | AST build related                 |
| config | Configuration for javaparser      |
| demo   | Demostratioon for the tool        |
| graph  | Outdated. Replaced by GraphWeaver |
| utils  | Collect some tools for use        |

# How to Use

We use openjdk version "1.8.0_392", run the whole system in a self-build platform with Ubuntu 20.04 and an I7-3770.
Higher device and Java version will also work. But I'm not sure if it works for other arch or system.
- Clone the project.
    - `git clone https://github.com/MartyLinZY/LogHound.git`
- Ensure the env para.
    - Java 8+
    - Maven
- Sync and build the project.
- Run the demo/CallGraphGenerator.