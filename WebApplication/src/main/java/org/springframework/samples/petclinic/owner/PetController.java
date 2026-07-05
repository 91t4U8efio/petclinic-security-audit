/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

	private static final Path ALLOWED_PHOTO_DIR = Paths.get(System.getProperty("java.io.tmpdir"));
	private static final Map<String, String> EXTENSION_MIME_TYPES = Map.of(
		"jpg", "image/jpeg",
		"jpeg", "image/jpeg",
		"png", "image/png",
		"gif", "image/gif",
		"bmp", "image/bmp",
		"webp", "image/webp"
	);
	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

	private final OwnerRepository owners;

	public PetController(OwnerRepository owners) {
		this.owners = owners;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.owners.findPetTypes();
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") int ownerId) {

		Owner owner = this.owners.findById(ownerId);
		if (owner == null) {
			throw new IllegalArgumentException("Owner ID not found: " + ownerId);
		}
		return owner;
	}

	@ModelAttribute("pet")
	public Pet findPet(@PathVariable("ownerId") int ownerId,
			@PathVariable(name = "petId", required = false) Integer petId) {

		Owner owner = this.owners.findById(ownerId);
		if (owner == null) {
			throw new IllegalArgumentException("Owner ID not found: " + ownerId);
		}
		return petId == null ? new Pet() : owner.getPet(petId);
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
	}

	@GetMapping("/pets/new")
	public String initCreationForm(Owner owner, ModelMap model) {
		Pet pet = new Pet();
		owner.addPet(pet);
		model.put("pet", pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/new")
	public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model) {
		if (StringUtils.hasText(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue("name", "duplicate", "already exists");
		}

		LocalDate currentDate = LocalDate.now();
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(currentDate)) {
			result.rejectValue("birthDate", "typeMismatch.birthDate");
		}

		owner.addPet(pet);
		if (result.hasErrors()) {
			model.put("pet", pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		this.owners.save(owner);
		return "redirect:/owners/{ownerId}";
	}

	@GetMapping("/pets/{petId}/edit")
	public String initUpdateForm(Owner owner, @PathVariable("petId") int petId, ModelMap model) {
		Pet pet = owner.getPet(petId);
		model.put("pet", pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/{petId}/edit")
	public String processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model) {

		String petName = pet.getName();

		// checking if the pet name already exist for the owner
		if (StringUtils.hasText(petName)) {
			Pet existingPet = owner.getPet(petName.toLowerCase(), false);
			if (existingPet != null && existingPet.getId() != pet.getId()) {
				result.rejectValue("name", "duplicate", "already exists");
			}
		}

		LocalDate currentDate = LocalDate.now();
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(currentDate)) {
			result.rejectValue("birthDate", "typeMismatch.birthDate");
		}

		if (result.hasErrors()) {
			model.put("pet", pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		owner.addPet(pet);
		this.owners.save(owner);
		return "redirect:/owners/{ownerId}";
	}



	@GetMapping("/pets/{petId}/uploadForm")
	public String showUploadForm(@PathVariable int ownerId, @PathVariable int petId, Model model) {
		// ... (add any necessary logic to prepare the model)
		return "pets/uploadForm"; // Create this Thymeleaf template
	}

	@PostMapping("/pets/{petId}/upload")
	public String handleFileUpload(@PathVariable int ownerId, @PathVariable int petId,
								   @RequestParam("file") MultipartFile file,
								   RedirectAttributes redirectAttributes) {
		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "Please select a file to upload.");
			return "redirect:/owners/{ownerId}/pets/{petId}/uploadForm";
		}

		if (file.getSize() > MAX_FILE_SIZE) {
			redirectAttributes.addFlashAttribute("message", "File size exceeds the maximum limit of 5 MB.");
			return "redirect:/owners/{ownerId}/pets/{petId}/uploadForm";
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			redirectAttributes.addFlashAttribute("message", "Only image files are allowed.");
			return "redirect:/owners/{ownerId}/pets/{petId}/uploadForm";
		}

		Owner owner = this.owners.findById(ownerId);
		if (owner == null) {
			throw new IllegalArgumentException("Owner ID not found: " + ownerId);
		}
		Pet pet = owner.getPet(petId);

		String originalFilename = file.getOriginalFilename();
		String extension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			String rawExt = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
			if (!ALLOWED_EXTENSIONS.contains(rawExt)) {
				redirectAttributes.addFlashAttribute("message", "File type not allowed.");
				return "redirect:/owners/{ownerId}/pets/{petId}/uploadForm";
			}
			extension = "." + rawExt;
		}
		String safeFileName = UUID.randomUUID() + extension;
		Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
		Path filePath = tmpDir.resolve(safeFileName).normalize();

		if (!filePath.startsWith(tmpDir)) {
			redirectAttributes.addFlashAttribute("message", "Invalid file path.");
			return "redirect:/owners/{ownerId}/pets/{petId}/uploadForm";
		}

		pet.setPhotoPath(filePath.toString());

		try {
			Files.copy(
				file.getInputStream(),
				filePath,
				StandardCopyOption.REPLACE_EXISTING);
			this.owners.save(owner);
		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("message", "An error occurred while uploading the file.");
			return "redirect:/owners/{ownerId}/pets/{petId}/uploadForm";
		}

		redirectAttributes.addFlashAttribute("message", "File uploaded successfully!");
		return "redirect:/owners/{ownerId}/pets/{petId}";
	}

	private String mimeTypeForPath(Path path) {
		String name = path.getFileName().toString().toLowerCase();
		int dotIdx = name.lastIndexOf('.');
		if (dotIdx > 0) {
			String ext = name.substring(dotIdx + 1);
			String mime = EXTENSION_MIME_TYPES.get(ext);
			if (mime != null) return mime;
		}
		return "application/octet-stream";
	}

	@GetMapping("/pets/getPhotoByPath")
	public void showImageByPath(@RequestParam String photoPath, HttpServletResponse response) throws IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not available");
	}


	@GetMapping("/pets/{petId}/image")
	public void showImage(@PathVariable int ownerId, @PathVariable int petId, HttpServletResponse response) throws IOException {
		Owner owner = this.owners.findById(ownerId);
		Pet pet = owner.getPet(petId);

		String imagePath = pet.getPhotoPath();
		if (imagePath != null) {
			Path path = Paths.get(imagePath).normalize();
			if (!path.startsWith(ALLOWED_PHOTO_DIR.toAbsolutePath().normalize())) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
				return;
			}
			if (!Files.exists(path) || !Files.isRegularFile(path)) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
				return;
			}
			byte[] imageBytes = Files.readAllBytes(path);
			response.setContentType(mimeTypeForPath(path));
			response.setHeader("X-Content-Type-Options", "nosniff");
			try (OutputStream os = response.getOutputStream()) {
				os.write(imageBytes);
				os.flush();
			}
		}
	}

}
