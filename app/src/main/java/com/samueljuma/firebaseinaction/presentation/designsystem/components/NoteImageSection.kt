package com.samueljuma.firebaseinaction.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.samueljuma.firebaseinaction.R
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme

@Composable
fun NoteImageSection(
    imageUrl: String?,
    isEditing: Boolean,
    isUploadingImage: Boolean,
    uploadProgress: Int?,
    onAddImageClicked: () -> Unit,
    onRemoveImageClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        when {
            isUploadingImage -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { (uploadProgress ?: 0) / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Uploading ${uploadProgress ?: 0}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            imageUrl != null -> {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_image_placeholder),
                        error = painterResource(R.drawable.ic_image_error),
                        contentDescription = "Note image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    if (isEditing) {
                        IconButton(
                            onClick = onRemoveImageClicked,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            isEditing -> {
                OutlinedButton(
                    onClick = onAddImageClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add cover image")
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NoteImageSectionUploadingPreview() {
    AppTheme {
        NoteImageSection(
            imageUrl = null,
            isEditing = true,
            isUploadingImage = true,
            uploadProgress = 47,
            onAddImageClicked = {},
            onRemoveImageClicked = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun NoteImageSectionImageEditModePreview() {
    AppTheme {
        NoteImageSection(
            imageUrl = "https://example.com/image.jpg",
            isEditing = true,
            isUploadingImage = false,
            uploadProgress = null,
            onAddImageClicked = {},
            onRemoveImageClicked = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun NoteImageSectionImageViewModePreview() {
    AppTheme {
        NoteImageSection(
            imageUrl = "https://example.com/image.jpg",
            isEditing = false,
            isUploadingImage = false,
            uploadProgress = null,
            onAddImageClicked = {},
            onRemoveImageClicked = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun NoteImageSectionNoImageEditModePreview() {
    AppTheme {
        NoteImageSection(
            imageUrl = null,
            isEditing = true,
            isUploadingImage = false,
            uploadProgress = null,
            onAddImageClicked = {},
            onRemoveImageClicked = {}
        )
    }
}
