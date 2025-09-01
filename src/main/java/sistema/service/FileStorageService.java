package sistema.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService() {
        this.fileStorageLocation = Paths.get("uploads")
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório para uploads.", ex);
        }
    }

    public String storeFile(MultipartFile file, String subdirectory, String userId) {
        try {
            Path userDirectory = this.fileStorageLocation.resolve(subdirectory).resolve(userId);
            Files.createDirectories(userDirectory);

            String fileName = UUID.randomUUID() + "." +
                    StringUtils.getFilenameExtension(file.getOriginalFilename());

            Path targetLocation = userDirectory.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subdirectory + "/" + userId + "/" + fileName;

        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível armazenar o arquivo.", ex);
        }
    }
}