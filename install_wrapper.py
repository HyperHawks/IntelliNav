import urllib.request
import os

print("Downloading gradle-wrapper.properties...")
os.makedirs("gradle/wrapper", exist_ok=True)
with open("gradle/wrapper/gradle-wrapper.properties", "w") as f:
    f.write("""distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
""")

print("Downloading gradlew...")
urllib.request.urlretrieve("https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradlew", "gradlew")
os.chmod("gradlew", 0o755)

print("Downloading gradle-wrapper.jar...")
urllib.request.urlretrieve("https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.jar")
print("Done.")
