package me.t65.reportgenapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import me.t65.reportgenapi.controller.payload.UidResponse;
import me.t65.reportgenapi.db.services.DbStatsService;
import me.t65.reportgenapi.utils.IdGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Statistics")
@RequestMapping(value = "/api/v1/stats")
@RestController
public class StatsApiController {

    private final DbStatsService dbStatsService;
    private final IdGenerator idGenerator;

    @Autowired
    public StatsApiController(DbStatsService dbStatsService, IdGenerator idGenerator) {
        this.dbStatsService = dbStatsService;
        this.idGenerator = idGenerator;
    }

    @Operation(
            summary = "Update statistic",
            description = "This endpoint updates the specified statistic")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Statistic edit successful"),
                @ApiResponse(responseCode = "404", description = "Unable to edit statistic"),
            })
    @PatchMapping("/{id}")
    public ResponseEntity<?> editStat(
            @Parameter(description = "The statistic id", required = true) @PathVariable("id")
                    String id,
            @RequestParam(name = "number") Integer number,
            @RequestParam(name = "title") String title,
            @RequestParam(name = "subtitle", required = false) String subtitle) {
        UUID uuid = UUID.fromString(id);

        boolean check = dbStatsService.editStat(uuid, number, title, subtitle);
        if (check) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Add new statistic",
            description = "This endpoint adds the specified statistic")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Add statistic successful.",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = UidResponse.class))),
                @ApiResponse(responseCode = "500", description = "Unable to add statistic"),
            })
    @PostMapping("/add")
    public ResponseEntity<?> addStat(
            @RequestParam(name = "number") Integer number,
            @RequestParam(name = "title") String title,
            @RequestParam(name = "subtitle", required = false) String subtitle) {
        UUID uuid = idGenerator.generateId();

        boolean check = dbStatsService.addStat(uuid, number, title, subtitle);
        if (check) {
            return ResponseEntity.ok(new UidResponse(uuid.toString()));
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }
}
