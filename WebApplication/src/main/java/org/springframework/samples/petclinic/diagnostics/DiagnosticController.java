package org.springframework.samples.petclinic.diagnostics;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/diagnostics")
public class DiagnosticController {

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/debug")
	public DiagnosticData showdebug() {
		Map<String, String> data = new HashMap<>();
		data.put("status", "ok");
		return new DiagnosticData(data);
	}

}
