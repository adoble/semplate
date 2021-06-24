# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html), i.e. version number has form  **MAJOR.MINOR.PATCH**;

* MAJOR version incremented for incompatible API changes,
* MINOR version incremented for new functionality that is backwards
  compatible
* PATCH version incremented for backwards compatible bug fixes.



## [Unreleased]

### Added
* Fluent interface added (SemanticWriter, SemanticReader).
* Existing markdown files that contain semantic information can be updated.
* A formal specification of the semantically marked up files added

### Changed
* New syntax for semantic markup added. The semantic block is now is at the start of a markdown block and the value of any field is directly extracted from the text itself rather than being in the semantic markdown.
* Generation of markdown files from templates using the new syntax
* Reading of markdown files (including lists) into data objects.
* New functionality to read templates that contain lists into data objects that have fields using a List<?> interface (e.g. ArrayList etc.).

## [0.1.0] - 2021-02-19

**Initial alpha release**

### Added
* Simple generation of a markdown file based on a template by using the data in
  a single data object
* Simple reading of a markdown file and creation of a data object using the semantic information in the markdown file.
