package com.samuelokello.mwenyeji.ui.theme.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

object MwenyejiIcons {
    // Navigation icons
    object Navigation {
        val home: ImageVector = Icons.Filled.Home
        val search: ImageVector = Icons.Filled.Search
        val profile: ImageVector = Icons.Filled.Person
        val settings: ImageVector = Icons.Filled.Settings
        val back: ImageVector = Icons.Filled.ArrowBack
        val close: ImageVector = Icons.Filled.Close
        val menu: ImageVector = Icons.Filled.Menu
    }

    // Action icons
    object Actions {
        val add: ImageVector = Icons.Filled.Add
        val delete: ImageVector = Icons.Filled.Delete
        val edit: ImageVector = Icons.Filled.Edit
        val share: ImageVector = Icons.Filled.Share
        val favorite: ImageVector = Icons.Filled.Favorite
        val favoriteBorder: ImageVector = Icons.Outlined.FavoriteBorder
        val send: ImageVector = Icons.Filled.Send
        val save: ImageVector = Icons.Filled.Save
        val download: ImageVector = Icons.Filled.Download
        val upload: ImageVector = Icons.Filled.Upload
        val refresh: ImageVector = Icons.Filled.Refresh
    }

    // Status icons
    object Status {
        val check: ImageVector = Icons.Filled.Check
        val checkCircle: ImageVector = Icons.Filled.CheckCircle
        val error: ImageVector = Icons.Filled.Error
        val warning: ImageVector = Icons.Filled.Warning
        val info: ImageVector = Icons.Filled.Info
    }

    // Content icons
    object Content {
        val image: ImageVector = Icons.Filled.Image
        val video: ImageVector = Icons.Filled.VideoLibrary
        val document: ImageVector = Icons.Filled.Description
        val folder: ImageVector = Icons.Filled.Folder
        val attachment: ImageVector = Icons.Filled.AttachFile
    }

    // Communication icons
    object Communication {
        val email: ImageVector = Icons.Filled.Email
        val phone: ImageVector = Icons.Filled.Phone
        val chat: ImageVector = Icons.Filled.Chat
        val notifications: ImageVector = Icons.Filled.Notifications
        val notificationsOff: ImageVector = Icons.Outlined.Notifications
    }

    // User icons
    object User {
        val person: ImageVector = Icons.Filled.Person
        val personAdd: ImageVector = Icons.Filled.PersonAdd
        val group: ImageVector = Icons.Filled.Group
        val accountCircle: ImageVector = Icons.Filled.AccountCircle
    }

    // Toggle icons
    object Toggle {
        val visibility: ImageVector = Icons.Filled.Visibility
        val visibilityOff: ImageVector = Icons.Filled.VisibilityOff
        val expandMore: ImageVector = Icons.Filled.ExpandMore
        val expandLess: ImageVector = Icons.Filled.ExpandLess
        val chevronRight: ImageVector = Icons.Filled.ChevronRight
        val chevronLeft: ImageVector = Icons.Filled.ChevronLeft
    }

    // Time & Date icons
    object Time {
        val calendar: ImageVector = Icons.Filled.CalendarToday
        val schedule: ImageVector = Icons.Filled.Schedule
        val timer: ImageVector = Icons.Filled.Timer
    }

    // Location icons
    object Location {
        val place: ImageVector = Icons.Filled.Place
        val map: ImageVector = Icons.Filled.Map
        val navigation: ImageVector = Icons.Filled.Navigation
    }
}
