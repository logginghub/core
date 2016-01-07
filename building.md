Now everything has been migrated over to gradle, building is less Heath-Robinson, but sadly is back to having a new manual steps...

1. gradle clean test     : to make sure everything is happy
2. git status            : to make sure you've got everything squared away
3. gradle install        : to deploy artifacts in the local maven repo, if you are building or compiling anything locally that needs the update
4. gradle distZip        : to build the deployable zip files
5. gradle uploadArchives : to deploy the artifacts and zip files to the remote repo at www.vertexlabs.co.uk

gradle install distZip uploadArchives

6. git tag [version number] : tag the release in git
7. Edit build.gradle and increase the version number
8. git commit -a -m "Ready for next version"
9. git push --tags

# Documentation

Generating the documentation is current a manual process, with a view to automating it in the future. The document generation is also a self-test that the version has been build and correctly deployed, so you can't run it until the artifacts are up on the server.

1. Search and replace in *documentation/* and replace the previous build number with the current build number
1. Run com.logginghub.documenter.Documenter in the documenter private project which should generate a new set of html files
1. Use your S3 client of choice - create the version number folder and upload the files
1. Remember to check the S3 client has the correct permissions to allow anyone to read the files