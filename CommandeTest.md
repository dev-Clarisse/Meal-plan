$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Back (depuis le dossier back)
./mvnw test                          # tous les tests
./mvnw test -Dtest=AuthServiceTest   # un seul fichier

# Front (depuis le dossier frontend)
ng test