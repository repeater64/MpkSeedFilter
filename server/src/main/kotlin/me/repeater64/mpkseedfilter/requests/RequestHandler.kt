package me.repeater64.mpkseedfilter.requests

import io.ktor.server.plugins.BadRequestException
import me.repeater64.mpkseedfilter.dto.request.BastionRequestInfo
import me.repeater64.mpkseedfilter.dto.request.EndRequestInfo
import me.repeater64.mpkseedfilter.dto.request.SeedType
import me.repeater64.mpkseedfilter.dto.request.SeedsRequest
import me.repeater64.mpkseedfilter.dto.request.SeedsRequestResponse
import me.repeater64.mpkseedfilter.filtering.database.LoadedSeedDatabase

object RequestHandler {

    @Throws(BadRequestException::class)
    fun handleRequest(request: SeedsRequest) : SeedsRequestResponse {
        val db = LoadedSeedDatabase.db
        val seeds = when (request.seedType) {
            SeedType.BASTION -> db.getSeedsBastion(request.userID, (request.additionalRequestInfo as? BastionRequestInfo)?.bastionInfo ?: throw BadRequestException("Can't request a Bastion seed without specifying bastion!"))
            SeedType.ENTRY_TO_BASTION -> db.getSeedsEntryToBastion(request.userID, (request.additionalRequestInfo as? BastionRequestInfo)?.bastionInfo ?: throw BadRequestException("Can't request an Entry to Bastion seed without specifying bastion!"))
            SeedType.BASTION_ONWARDS -> db.getSeedsBastionOnwards(request.userID, (request.additionalRequestInfo as? BastionRequestInfo)?.bastionInfo ?: throw BadRequestException("Can't request a Bastion Onwards seed without specifying bastion!"), request.fortOpennessBounds?.first ?: 0.0, request.fortOpennessBounds?.second ?: 1.0)
            SeedType.ENTRY_ONWARDS -> db.getSeedsEntryOnwards(request.userID, (request.additionalRequestInfo as? BastionRequestInfo)?.bastionInfo ?: throw BadRequestException("Can't request an Entry Onwards seed without specifying bastion!"), request.fortOpennessBounds?.first ?: 0.0, request.fortOpennessBounds?.second ?: 1.0)
            SeedType.FORT_ONWARDS -> db.getSeedsFortOnwards(request.userID, request.fortOpennessBounds?.first ?: 0.0, request.fortOpennessBounds?.second ?: 1.0)
            SeedType.ENDGAME -> db.getSeedsEndgame(request.userID, (request.additionalRequestInfo as? EndRequestInfo)?.endInfo ?: throw BadRequestException("Can't request an Endgame seed without specifying end requirements!"))
        }
        return SeedsRequestResponse(seeds.mapNotNull { db.seedInfo[it] })
    }
}